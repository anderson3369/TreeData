//
//  SettingsView.swift
//  TreeData
//
//  Created by Steven Anderson on 5/3/26.
//

import SwiftUI
import shared

struct SettingsView: View {
    @State private var authManager = AuthManager.shared
    
    var body: some View {
        NavigationView {
            List {
                Section(header: Text("Sign In")) {
                    if authManager.isSignedIn {
                        HStack {
                            Image(systemName: "person.crop.circle.fill")
                                .foregroundStyle(.orchardPrimary)
                            VStack(alignment: .leading) {
                                if !authManager.displayName.isEmpty {
                                    Text(authManager.displayName)
                                        .font(.headline)
                                }
                                if !authManager.email.isEmpty {
                                    Text(authManager.email)
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                            }
                        }
                        Button("Sign Out", role: .destructive) {
                            authManager.signOut()
                        }
                    } else {
                        NavigationLink {
                            SignInView()
                        } label: {
                            Label("Sign In", systemImage: "person.badge.key.fill")
                        }
                    }
                }

                Section(header: Text("Account")) {
                    NavigationLink {
                        FarmerView()
                    } label: {
                        Label("Farmer", systemImage: "person.fill")
                    }
                }

                Section(header: Text("Property")) {
                    NavigationLink {
                        FarmView()
                    } label: {
                        Label("Farms", systemImage: "house.fill")
                    }

                    NavigationLink {
                        OrchardView()
                    } label: {
                        Label("Orchards", systemImage: "leaf.fill")
                    }
                }

                Section(header: Text("Equipment")) {
                    NavigationLink {
                        PumpView()
                    } label: {
                        Label("Pumps", systemImage: "engine.combustion.fill")
                    }
                }

                if authManager.isSignedIn {
                    Section(header: Text("Sharing")) {
                        NavigationLink {
                            InviteCodeView()
                        } label: {
                            Label("Invite to Farm", systemImage: "person.badge.plus")
                        }

                        NavigationLink {
                            JoinFarmView()
                        } label: {
                            Label("Join a Farm", systemImage: "person.2.fill")
                        }
                    }
                }
            }
            .navigationTitle("Settings")
        }
    }
}

struct InviteCodeView: View {
    @State private var code: String = ""
    @State private var errorMessage: String?
    @State private var isGenerating = false

    private var siteId: String { FirestoreSync.shared.currentFarmSiteId }

    var body: some View {
        VStack(spacing: 20) {
            if siteId.isEmpty {
                Text("No farm is selected. Add a farm first, then come back here to invite someone.")
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                    .padding()
            } else if code.isEmpty {
                Text("Generate a share code for site \(siteId).")
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
                Button {
                    generate()
                } label: {
                    Label("Generate Code", systemImage: "key.fill")
                }
                .buttonStyle(.borderedProminent)
                .disabled(isGenerating)
            } else {
                Text("Share this code")
                    .font(.headline)
                Text(code)
                    .font(.system(.largeTitle, design: .monospaced))
                    .fontWeight(.bold)
                    .textSelection(.enabled)
                Text("Expires in 7 days")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                ShareLink(item: code) {
                    Label("Share Code", systemImage: "square.and.arrow.up")
                }
                .buttonStyle(.borderedProminent)
                Button("Generate Another") {
                    code = ""
                }
                .buttonStyle(.bordered)
            }

            if let errorMessage {
                Text(errorMessage)
                    .foregroundStyle(.red)
                    .font(.caption)
                    .padding(.horizontal)
            }
            Spacer()
        }
        .padding(.top, 40)
        .navigationTitle("Invite to Farm")
    }

    private func generate() {
        let currentSiteId = siteId
        guard !currentSiteId.isEmpty else { return }
        isGenerating = true
        errorMessage = nil
        Task {
            do {
                let generated = try await FirestoreSync.shared.createInvite(siteId: currentSiteId)
                await MainActor.run {
                    code = generated
                    isGenerating = false
                }
            } catch {
                await MainActor.run {
                    errorMessage = error.localizedDescription
                    isGenerating = false
                }
            }
        }
    }
}

struct JoinFarmView: View {
    @State private var enteredCode: String = ""
    @State private var status: String?
    @State private var isError: Bool = false
    @State private var isJoining: Bool = false

    var body: some View {
        VStack(spacing: 16) {
            Text("Enter the share code you were given.")
                .multilineTextAlignment(.center)
                .padding(.horizontal)

            TextField("e.g. ABC234", text: $enteredCode)
                .textInputAutocapitalization(.characters)
                .autocorrectionDisabled(true)
                .font(.system(.title2, design: .monospaced))
                .multilineTextAlignment(.center)
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(10)
                .padding(.horizontal)

            Button {
                join()
            } label: {
                if isJoining {
                    ProgressView()
                } else {
                    Label("Join Farm", systemImage: "checkmark.circle.fill")
                }
            }
            .buttonStyle(.borderedProminent)
            .disabled(enteredCode.trimmingCharacters(in: .whitespaces).isEmpty || isJoining)

            if let status {
                Text(status)
                    .foregroundStyle(isError ? .red : .green)
                    .font(.caption)
                    .padding(.horizontal)
            }

            Spacer()
        }
        .padding(.top, 40)
        .navigationTitle("Join a Farm")
    }

    private func join() {
        let code = enteredCode
        isJoining = true
        status = nil
        Task {
            do {
                let siteId = try await FirestoreSync.shared.redeemInvite(code: code)
                await MainActor.run {
                    status = "Joined farm \(siteId). New data will sync automatically."
                    isError = false
                    isJoining = false
                    enteredCode = ""
                }
            } catch {
                await MainActor.run {
                    status = error.localizedDescription
                    isError = true
                    isJoining = false
                }
            }
        }
    }
}
