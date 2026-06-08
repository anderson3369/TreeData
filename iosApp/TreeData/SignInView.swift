//
//  SignInView.swift
//  TreeData
//
//  Created by Claude on 5/15/26.
//

import SwiftUI
import AuthenticationServices
import CryptoKit
import FirebaseAuth
import shared

@Observable
class AuthManager {
    static let shared = AuthManager()
    
    var isSignedIn: Bool = false
    var displayName: String = ""
    var email: String = ""
    
    private var authStateHandle: AuthStateDidChangeListenerHandle?
    
    private init() {
        authStateHandle = Auth.auth().addStateDidChangeListener { [weak self] _, user in
            DispatchQueue.main.async {
                self?.isSignedIn = user != nil
                self?.displayName = user?.displayName ?? ""
                self?.email = user?.email ?? ""
            }
        }
    }
    
    func signOut() {
        try? Auth.auth().signOut()
    }
}

struct SignInView: View {
    @State private var authManager = AuthManager.shared
    @State private var currentNonce: String?
    @State private var errorMessage: String?
    
    var body: some View {
        if authManager.isSignedIn {
            signedInContent
        } else {
            signInContent
        }
    }
    
    private var signedInContent: some View {
        VStack(spacing: 16) {
            Image(systemName: "person.crop.circle.fill")
                .font(.system(size: 60))
                .foregroundStyle(.orchardPrimary)
            
            Text("Signed In")
                .font(.title2)
                .fontWeight(.bold)
            
            if !authManager.displayName.isEmpty {
                Text(authManager.displayName)
                    .font(.headline)
                    .foregroundStyle(.secondary)
            }
            
            if !authManager.email.isEmpty {
                Text(authManager.email)
                    .font(.subheadline)
                    .foregroundStyle(.tertiary)
            }
            
            Button("Sign Out") {
                authManager.signOut()
            }
            .buttonStyle(.bordered)
            .tint(.red)
            .padding(.top, 8)
        }
        .padding()
    }
    
    private var signInContent: some View {
        VStack(spacing: 24) {
            Image(systemName: "person.crop.circle")
                .font(.system(size: 60))
                .foregroundStyle(.orchardPrimary)
            
            Text("Sign In")
                .font(.title2)
                .fontWeight(.bold)
            
            Text("Sign in to sync your orchard data across devices")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
            
            if let errorMessage {
                Text(errorMessage)
                    .font(.caption)
                    .foregroundStyle(.red)
                    .multilineTextAlignment(.center)
            }
            
            SignInWithAppleButton(.signIn) { request in
                let nonce = randomNonceString()
                currentNonce = nonce
                request.requestedScopes = [.fullName, .email]
                request.nonce = sha256(nonce)
            } onCompletion: { result in
                handleAppleSignIn(result)
            }
            .signInWithAppleButtonStyle(.black)
            .frame(height: 50)
            .cornerRadius(12)
        }
        .padding(32)
    }
    
    private func handleAppleSignIn(_ result: Result<ASAuthorization, Error>) {
        switch result {
        case .success(let authorization):
            guard let appleIDCredential = authorization.credential as? ASAuthorizationAppleIDCredential,
                  let appleIDToken = appleIDCredential.identityToken,
                  let idTokenString = String(data: appleIDToken, encoding: .utf8),
                  let nonce = currentNonce else {
                errorMessage = "Unable to get Apple ID token"
                return
            }
            
            // Sign in via the shared AuthService so GitLive's Firebase Kotlin SDK
            // (used by FirestoreSync) sees the authenticated user. Calling native
            // Auth.auth().signIn directly leaves the shared SDK's currentUser as nil.
            Task {
                do {
                    let result = try await AuthService.shared.signInWithAppleCredential(
                        idToken: idTokenString,
                        nonce: nonce
                    )
                    if result.boolValue {
                        await MainActor.run { errorMessage = nil }
                        let db = DatabaseManager.shared.getDatabase()
                        try? await FirestoreSync.shared.pushAllLocalData(db: db)
                    } else {
                        await MainActor.run { errorMessage = "Sign-in failed" }
                    }
                } catch {
                    await MainActor.run { errorMessage = error.localizedDescription }
                }
            }
            
        case .failure(let error):
            errorMessage = error.localizedDescription
        }
    }
    
    private func randomNonceString(length: Int = 32) -> String {
        precondition(length > 0)
        var randomBytes = [UInt8](repeating: 0, count: length)
        let errorCode = SecRandomCopyBytes(kSecRandomDefault, randomBytes.count, &randomBytes)
        if errorCode != errSecSuccess {
            fatalError("Unable to generate nonce. SecRandomCopyBytes failed with OSStatus \(errorCode)")
        }
        let charset: [Character] = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        return String(randomBytes.map { charset[Int($0) % charset.count] })
    }
    
    private func sha256(_ input: String) -> String {
        let inputData = Data(input.utf8)
        let hashedData = SHA256.hash(data: inputData)
        return hashedData.compactMap { String(format: "%02x", $0) }.joined()
    }
}
