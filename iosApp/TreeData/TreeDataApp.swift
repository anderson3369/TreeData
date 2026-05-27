//
//  TreeDataApp.swift
//  TreeData
//
//  Created by Steven Anderson on 3/14/25.
//

import SwiftUI
import shared
import FirebaseCore

@main
struct TreeDataApp: App {
    @State private var isSetupComplete = false
    
    // Check existing data on launch
    private let farmerViewModel = ViewModelProvider.shared.farmerViewModel
    private let farmViewModel = ViewModelProvider.shared.farmViewModel
    private let orchardViewModel = ViewModelProvider.shared.orchardViewModel

    init() {
        FirebaseApp.configure()
    }

    var body: some Scene {
        WindowGroup {
            if isSetupComplete {
                MainTabView()
            } else {
                SetupCheckView(isSetupComplete: $isSetupComplete)
            }
        }
    }
}

/// Checks whether Farmer/Farm/Orchard data exists.
/// If all exist, immediately shows the main app.
/// Otherwise, shows the setup wizard.
struct SetupCheckView: View {
    @Binding var isSetupComplete: Bool
    
    private let farmerViewModel = ViewModelProvider.shared.farmerViewModel
    private let farmViewModel = ViewModelProvider.shared.farmViewModel
    private let orchardViewModel = ViewModelProvider.shared.orchardViewModel
    
    @State private var hasChecked = false
    @State private var needsSetup = false
    
    var body: some View {
        Group {
            if !hasChecked {
                ProgressView("Loading...")
                    .tint(.orchardPrimary)
            } else if needsSetup {
                SetupWizardView(isSetupComplete: $isSetupComplete)
            }
        }
        .task { checkData() }
    }
    
    private func checkData() {
        let farmersFlow = farmerViewModel.farmers as! CommonStateFlow<NSArray>
        farmersFlow.subscribe { farmerList in
            DispatchQueue.main.async {
                let farmers = (farmerList as? [Farmer]) ?? []
                
                let farmsFlow = self.farmViewModel.farms as! CommonStateFlow<NSArray>
                farmsFlow.subscribe { farmList in
                    DispatchQueue.main.async {
                        let farms = (farmList as? [Farm]) ?? []
                        
                        let orchardsFlow = self.orchardViewModel.allOrchards as! CommonStateFlow<NSArray>
                        orchardsFlow.subscribe { orchardList in
                            DispatchQueue.main.async {
                                let orchards = (orchardList as? [Orchard]) ?? []
                                
                                if !farmers.isEmpty && !farms.isEmpty && !orchards.isEmpty {
                                    self.isSetupComplete = true
                                } else {
                                    self.needsSetup = true
                                }
                                self.hasChecked = true
                            }
                        }
                    }
                }
            }
        }
    }
}

struct MainTabView: View {
    var body: some View {
        TabView {
            OrchardActivityView().tabItem {
                Label("Activities", systemImage: "leaf.fill")
            }

            TreeView().tabItem {
                Label("Trees", systemImage: "tree.fill")
            }

            FertilizerView().tabItem {
                Label("Fertilizers", systemImage: "drop.fill")
            }

            PesticideView().tabItem {
                Label("Pesticides", systemImage: "ant.fill")
            }

            IrrigationView().tabItem {
                Label("Irrigation", systemImage: "water.waves")
            }

            SoilMoistureView().tabItem {
                Label("Moisture", systemImage: "gauge.with.dots.needle.bottom.100percent")
            }

            SettingsView().tabItem {
                Label("Settings", systemImage: "gearshape.fill")
            }
        }
        .tint(.orchardPrimary)
    }
}
