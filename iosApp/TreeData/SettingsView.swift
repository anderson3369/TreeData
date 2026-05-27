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
            }
            .navigationTitle("Settings")
        }
    }
}
