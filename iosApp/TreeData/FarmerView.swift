//
//  ContentView.swift
//  TreeData
//
//  Created by Steven Anderson on 3/14/25.
//

import SwiftUI
import shared

struct FarmerView: View {
    // 1. Get the shared singleton ViewModel
    private let viewModel = ViewModelProvider.shared.farmerViewModel
    
    // 2. Local state for form fields
    @State private var name: String = ""
    @State private var address: String = ""
    @State private var city: String = ""
    @State private var state: String = ""
    @State private var zip: String = ""
    @State private var phone: String = ""
    @State private var email: String = ""
    
    @State private var farmers: [Farmer] = []
    @State private var selectedFarmerId: Int64 = 0
    @State private var statusMessage: String = ""

    var body: some View {
        Form {
            Section(header: Text("Farmer Information")) {
                TextField("Name", text: $name)
                TextField("Address", text: $address)
                TextField("City", text: $city)
                TextField("State", text: $state)
                TextField("Zip", text: $zip)
                TextField("Phone", text: $phone)
                    .keyboardType(.phonePad)
                TextField("Email", text: $email)
                    .keyboardType(.emailAddress)
                    .autocapitalization(.none)
            }

            Section {
                Button(action: saveFarmer) {
                    Text("Save Farmer")
                        .frame(maxWidth: .infinity)
                        .fontWeight(.bold)
                }
                .buttonStyle(.borderedProminent)
                .tint(.orchardPrimary)
            }

            if !statusMessage.isEmpty {
                Section {
                    Text(statusMessage)
                        .foregroundStyle(.secondary)
                        .font(.caption)
                }
            }
        }
        .navigationTitle("Farmer")
        .task { observeData() }
    }

    private func saveFarmer() {
        // 3. Construct the shared Entity
        let newFarmer = Farmer(
            id: 0, // Auto-generated in Room
            name: name,
            address: address,
            city: city,
            state: state,
            zip: zip,
            phone: phone,
            email: email
        )
        
        // 4. Call the ViewModel (which uses the Repository internally)
        viewModel.addFarmer(farmer: newFarmer)
        
        statusMessage = "Farmer \(name) saved to shared database!"
        clearForm()
    }
    
    private func clearForm() {
        name = ""
        address = ""
        city = ""
        state = ""
        zip = ""
        phone = ""
        email = ""
    }
    
    private func observeData() {
        // We subscribe to the flow and update the @State list
        let farmersFlow = viewModel.farmers as! CommonStateFlow<NSArray>
        farmersFlow.subscribe { farmerList in
            DispatchQueue.main.async {
                self.farmers = (farmerList as? [Farmer]) ?? []
                if selectedFarmerId == 0, let first = self.farmers.first {
                    selectedFarmerId = first.id
                }
            }
        }
    }
}

#Preview {
    FarmerView()
}
