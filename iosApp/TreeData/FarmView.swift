//
//  FarmView.swift
//  TreeData
//
//  Created by Steven Anderson on 4/3/26.
//


import SwiftUI
import shared

struct FarmView: View {
    // 1. Get shared ViewModels
    private let farmViewModel = ViewModelProvider.shared.farmViewModel
    private let farmerViewModel = ViewModelProvider.shared.farmerViewModel
    
    // 2. Local State
    @State private var farmName: String = ""
    @State private var siteId: String = ""
    @State private var selectedFarm: Farm? = nil
    
    // For observing data from Kotlin StateFlow
    @State private var farms: [Farm] = []
    @State private var farmers: [Farmer] = []
    @State private var selectedFarmerId: Int64 = 0
    
    var body: some View {
        Form {
            Section(header: Text("Existing Farms")) {
                Picker("Select a Farm", selection: $selectedFarm) {
                    Text("New Farm").tag(nil as Farm?)
                    ForEach(farms, id: \.id) { farm in
                        Text(farm.name).tag(farm as Farm?)
                    }
                }
                .onChange(of: selectedFarm) { newValue in
                    populateFields(from: newValue)
                }
            }

            Section(header: Text("Farm Details")) {
                Picker("Select Farmer", selection: $selectedFarmerId) {
                    if farmers.isEmpty {
                        Text("No Farmers Found").tag(Int64(0))
                    }
                    ForEach(farmers, id: \.id) { farmer in
                        Text(farmer.name).tag(farmer.id)
                    }
                }

                TextField("Farm Name", text: $farmName)
                TextField("Site ID", text: $siteId)
            }

            Section {
                HStack {
                    Button("Save") { saveFarm() }
                        .buttonStyle(.borderedProminent)
                        .tint(.orchardPrimary)

                    Spacer()

                    Button("New") { resetForm() }
                        .buttonStyle(.bordered)

                    Spacer()

                    Button("Delete", role: .destructive) { deleteFarm() }
                        .buttonStyle(.bordered)
                        .disabled(selectedFarm == nil)
                }
            }
        }
        .navigationTitle("Farms")
        .task { observeData() }
    }
    
    private func observeData() {
        let farmsFlow = farmViewModel.farms as! CommonStateFlow<NSArray>
        farmsFlow.subscribe { farmList in
            DispatchQueue.main.async {
                self.farms = (farmList as? [Farm]) ?? []
            }
        }

        let farmersFlow = farmerViewModel.farmers as! CommonStateFlow<NSArray>
        farmersFlow.subscribe { farmerList in
            DispatchQueue.main.async {
                let list = (farmerList as? [Farmer]) ?? []
                self.farmers = list
                if selectedFarmerId == 0, let first = list.first {
                    selectedFarmerId = first.id
                }
            }
        }
    }
    
    private func populateFields(from farm: Farm?) {
        if let farm = farm {
            farmName = farm.name
            siteId = farm.siteId
            selectedFarmerId = farm.farmerId
        } else {
            resetForm()
        }
    }
    
    private func resetForm() {
        selectedFarm = nil
        farmName = ""
        siteId = ""
    }
    
    private func saveFarm() {
        guard selectedFarmerId != 0 else { return }
        
        let farm = Farm(
            id: selectedFarm?.id ?? 0,
            farmerId: selectedFarmerId,
            name: farmName,
            siteId: siteId,
            persistentId: selectedFarm?.persistentId ?? UUID().uuidString,
            validFrom: selectedFarm?.validFrom ?? Date().toKotlinInstant(),
            validTo: nil
        )
        
        if farm.id > 0 {
            farmViewModel.updateFarm(farm: farm)
        } else {
            farmViewModel.addFarm(farm: farm)
        }
        resetForm()
    }
    
    private func deleteFarm() {
        if let farm = selectedFarm {
            farmViewModel.deleteFarm(farm: farm)
            resetForm()
        }
    }
}
