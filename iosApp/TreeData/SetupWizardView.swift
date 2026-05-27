//
//  SetupWizardView.swift
//  TreeData
//
//  Created by Steven Anderson on 4/26/26.
//

import SwiftUI
import shared

/// Setup wizard that guides first-time users through creating
/// their Farmer, Farm, and Orchard records.
///
/// Design:
/// - Step-based state machine (WizardStep enum)
/// - Skips completed steps on resume
/// - Future: add .importData case for Firestore import
/// - Future: cancel button to skip wizard and import instead

enum WizardStep: Int, CaseIterable {
    case farmer = 0
    case farm = 1
    case orchard = 2
    // Future: case importData = 3

    var title: String {
        switch self {
        case .farmer: return "Farmer"
        case .farm: return "Farm"
        case .orchard: return "Orchard"
        }
    }
}

struct SetupWizardView: View {
    private let farmerViewModel = ViewModelProvider.shared.farmerViewModel
    private let farmViewModel = ViewModelProvider.shared.farmViewModel
    private let orchardViewModel = ViewModelProvider.shared.orchardViewModel

    @Binding var isSetupComplete: Bool

    @State private var currentStep: WizardStep = .farmer
    @State private var farmers: [Farmer] = []
    @State private var farms: [Farm] = []
    @State private var orchards: [Orchard] = []
    @State private var savedFarmerId: Int64 = 0
    @State private var savedFarmId: Int64 = 0

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Step indicator
                StepIndicatorView(currentStep: currentStep)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 16)

                // Step content
                Group {
                    switch currentStep {
                    case .farmer:
                        FarmerStepView(
                            farmerViewModel: farmerViewModel,
                            existingFarmer: farmers.first,
                            onNext: { farmerId in
                                savedFarmerId = farmerId
                                withAnimation { currentStep = .farm }
                            }
                        )
                    case .farm:
                        FarmStepView(
                            farmViewModel: farmViewModel,
                            farmerId: savedFarmerId,
                            existingFarm: farms.first,
                            onBack: { withAnimation { currentStep = .farmer } },
                            onNext: { farmId in
                                savedFarmId = farmId
                                withAnimation { currentStep = .orchard }
                            }
                        )
                    case .orchard:
                        OrchardStepView(
                            orchardViewModel: orchardViewModel,
                            farmId: savedFarmId,
                            existingOrchard: orchards.first,
                            onBack: { withAnimation { currentStep = .farm } },
                            onComplete: { isSetupComplete = true }
                        )
                    }
                }
                .transition(.asymmetric(
                    insertion: .move(edge: .trailing).combined(with: .opacity),
                    removal: .move(edge: .leading).combined(with: .opacity)
                ))
            }
            .navigationTitle("Setup Your Orchard")
            .navigationBarTitleDisplayMode(.inline)
            // Future: add toolbar item for Import/Cancel
            // .toolbar {
            //     ToolbarItem(placement: .navigationBarTrailing) {
            //         Button("Import") { /* navigate to Firestore import */ }
            //     }
            // }
        }
        .task { observeData() }
    }

    private func observeData() {
        let farmersFlow = farmerViewModel.farmers as! CommonStateFlow<NSArray>
        farmersFlow.subscribe { list in
            DispatchQueue.main.async {
                self.farmers = (list as? [Farmer]) ?? []
                // Skip to first incomplete step
                if !self.farmers.isEmpty && self.currentStep == .farmer {
                    self.savedFarmerId = self.farmers.first!.id
                    if self.farms.isEmpty {
                        self.currentStep = .farm
                    }
                }
            }
        }

        let farmsFlow = farmViewModel.farms as! CommonStateFlow<NSArray>
        farmsFlow.subscribe { list in
            DispatchQueue.main.async {
                self.farms = (list as? [Farm]) ?? []
                if !self.farms.isEmpty && self.currentStep == .farm {
                    self.savedFarmId = self.farms.first!.id
                    if self.orchards.isEmpty {
                        self.currentStep = .orchard
                    }
                }
            }
        }

        let orchardsFlow = orchardViewModel.allOrchards as! CommonStateFlow<NSArray>
        orchardsFlow.subscribe { list in
            DispatchQueue.main.async {
                self.orchards = (list as? [Orchard]) ?? []
                if !self.farmers.isEmpty && !self.farms.isEmpty && !self.orchards.isEmpty {
                    self.isSetupComplete = true
                }
            }
        }
    }
}

// MARK: - Step Indicator

struct StepIndicatorView: View {
    let currentStep: WizardStep

    var body: some View {
        HStack {
            ForEach(Array(WizardStep.allCases.enumerated()), id: \.element) { index, step in
                let isCompleted = step.rawValue < currentStep.rawValue
                let isCurrent = step == currentStep

                VStack(spacing: 4) {
                    ZStack {
                        Circle()
                            .fill(isCompleted ? Color.orchardPrimary : (isCurrent ? Color.orchardSecondary : Color.gray.opacity(0.3)))
                            .frame(width: 36, height: 36)

                        if isCompleted {
                            Image(systemName: "checkmark")
                                .foregroundColor(.white)
                                .font(.system(size: 14, weight: .bold))
                        } else {
                            Text("\(step.rawValue + 1)")
                                .foregroundColor(.white)
                                .font(.system(size: 14, weight: .bold))
                        }
                    }

                    Text(step.title)
                        .font(.caption)
                        .fontWeight(isCurrent ? .bold : .regular)
                        .foregroundColor(isCurrent ? .orchardSecondary : .gray)
                }

                if index < WizardStep.allCases.count - 1 {
                    Rectangle()
                        .fill(isCompleted ? Color.orchardPrimary : Color.gray.opacity(0.3))
                        .frame(height: 2)
                        .frame(maxWidth: .infinity)
                        .padding(.horizontal, 4)
                        .offset(y: -10)
                }
            }
        }
    }
}

// MARK: - Step 1: Farmer

struct FarmerStepView: View {
    let farmerViewModel: FarmerViewModel
    let existingFarmer: Farmer?
    let onNext: (Int64) -> Void

    @State private var name: String = ""
    @State private var address: String = ""
    @State private var city: String = ""
    @State private var state: String = ""
    @State private var zip: String = ""
    @State private var phone: String = ""
    @State private var email: String = ""
    @State private var farmers: [Farmer] = []
    @State private var waitingForSave = false

    var body: some View {
        Form {
            Section {
                Text("Tell us about yourself")
                    .font(.title2.bold())
                Text("We need your basic information to get started.")
                    .foregroundStyle(.secondary)
                    .font(.subheadline)
            }
            .listRowBackground(Color.clear)

            Section(header: Text("Farmer Information")) {
                TextField("Name *", text: $name)
                TextField("Address", text: $address)
                HStack {
                    TextField("City", text: $city)
                    TextField("State", text: $state)
                        .frame(maxWidth: 80)
                }
                TextField("Zip", text: $zip)
                    .keyboardType(.numberPad)
                TextField("Phone", text: $phone)
                    .keyboardType(.phonePad)
                TextField("Email", text: $email)
                    .keyboardType(.emailAddress)
                    .autocapitalization(.none)
            }

            Section {
                Button(action: saveFarmer) {
                    Text("Next: Add Your Farm →")
                        .frame(maxWidth: .infinity)
                        .fontWeight(.bold)
                }
                .buttonStyle(.borderedProminent)
                .tint(.orchardPrimary)
                .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)
            }
        }
        .onAppear {
            if let farmer = existingFarmer {
                name = farmer.name
                address = farmer.address
                city = farmer.city
                state = farmer.state
                zip = farmer.zip
                phone = farmer.phone
                email = farmer.email
            }
            observeFarmers()
        }
    }

    private func observeFarmers() {
        let flow = farmerViewModel.farmers as! CommonStateFlow<NSArray>
        flow.subscribe { list in
            DispatchQueue.main.async {
                self.farmers = (list as? [Farmer]) ?? []
                if self.waitingForSave, let first = self.farmers.first {
                    self.waitingForSave = false
                    self.onNext(first.id)
                }
            }
        }
    }

    private func saveFarmer() {
        let farmer = Farmer(
            id: existingFarmer?.id ?? 0,
            name: name, address: address, city: city,
            state: state, zip: zip, phone: phone, email: email
        )
        if farmer.id > 0 {
            farmerViewModel.updateFarmer(farmer: farmer)
            onNext(farmer.id)
        } else {
            farmerViewModel.addFarmer(farmer: farmer)
            waitingForSave = true
        }
    }
}

// MARK: - Step 2: Farm

struct FarmStepView: View {
    let farmViewModel: FarmViewModel
    let farmerId: Int64
    let existingFarm: Farm?
    let onBack: () -> Void
    let onNext: (Int64) -> Void

    @State private var farmName: String = ""
    @State private var siteId: String = ""
    @State private var farms: [Farm] = []
    @State private var waitingForSave = false

    var body: some View {
        Form {
            Section {
                Text("Where is your farm?")
                    .font(.title2.bold())
                Text("Give your farm a name and optional site ID.")
                    .foregroundStyle(.secondary)
                    .font(.subheadline)
            }
            .listRowBackground(Color.clear)

            Section(header: Text("Farm Details")) {
                TextField("Farm Name *", text: $farmName)
                TextField("Site ID (optional)", text: $siteId)
            }

            Section {
                HStack(spacing: 12) {
                    Button(action: onBack) {
                        Text("← Back")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)

                    Button(action: saveFarm) {
                        Text("Next: Add Orchard →")
                            .frame(maxWidth: .infinity)
                            .fontWeight(.bold)
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(.orchardPrimary)
                    .disabled(farmName.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
        .onAppear {
            if let farm = existingFarm {
                farmName = farm.name
                siteId = farm.siteId
            }
            observeFarms()
        }
    }

    private func observeFarms() {
        let flow = farmViewModel.farms as! CommonStateFlow<NSArray>
        flow.subscribe { list in
            DispatchQueue.main.async {
                self.farms = (list as? [Farm]) ?? []
                if self.waitingForSave, let first = self.farms.first {
                    self.waitingForSave = false
                    self.onNext(first.id)
                }
            }
        }
    }

    private func saveFarm() {
        let farm = Farm(
            id: existingFarm?.id ?? 0,
            persistentId: existingFarm?.persistentId ?? UUID().uuidString,
            farmerId: farmerId,
            name: farmName,
            siteId: siteId,
            validFrom: existingFarm?.validFrom ?? Date().toKotlinInstant(),
            validTo: nil
        )
        if farm.id > 0 {
            farmViewModel.updateFarm(farm: farm)
            onNext(farm.id)
        } else {
            farmViewModel.addFarm(farm: farm)
            waitingForSave = true
        }
    }
}

// MARK: - Step 3: Orchard

struct OrchardStepView: View {
    let orchardViewModel: OrchardViewModel
    let farmId: Int64
    let existingOrchard: Orchard?
    let onBack: () -> Void
    let onComplete: () -> Void

    @State private var crop: String = ""
    @State private var plantedDate: Date = Date()
    @State private var rowWidth: String = ""
    @State private var rowWidthUnit: LinearUnit = .feet
    @State private var distanceBetweenTrees: String = ""
    @State private var distanceUnit: LinearUnit = .feet
    @State private var orchards: [Orchard] = []
    @State private var waitingForSave = false

    private let linearUnits: [LinearUnit] = [.feet, .inches, .meters]

    var body: some View {
        Form {
            Section {
                Text("What are you growing?")
                    .font(.title2.bold())
                Text("Tell us about your first orchard or crop. You can add soil details later.")
                    .foregroundStyle(.secondary)
                    .font(.subheadline)
            }
            .listRowBackground(Color.clear)

            Section(header: Text("Orchard Details")) {
                TextField("Crop *", text: $crop)
                DatePicker("Planted Date", selection: $plantedDate, displayedComponents: .date)
            }

            Section(header: Text("Spacing (optional)")) {
                HStack {
                    TextField("Row Width", text: $rowWidth)
                        .keyboardType(.decimalPad)
                    Picker("Unit", selection: $rowWidthUnit) {
                        ForEach(linearUnits, id: \.self) { unit in
                            Text(unit.description()).tag(unit)
                        }
                    }
                    .pickerStyle(.menu)
                }
                HStack {
                    TextField("Tree Spacing", text: $distanceBetweenTrees)
                        .keyboardType(.decimalPad)
                    Picker("Unit", selection: $distanceUnit) {
                        ForEach(linearUnits, id: \.self) { unit in
                            Text(unit.description()).tag(unit)
                        }
                    }
                    .pickerStyle(.menu)
                }
            }

            Section {
                HStack(spacing: 12) {
                    Button(action: onBack) {
                        Text("← Back")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)

                    Button(action: saveOrchard) {
                        Text("Done ✓")
                            .frame(maxWidth: .infinity)
                            .fontWeight(.bold)
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(.orchardPrimary)
                    .disabled(crop.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
        .onAppear {
            if let orchard = existingOrchard {
                crop = orchard.crop
                let dateFormatter = DateFormatter()
                dateFormatter.dateFormat = "yyyy-MM-dd"
                plantedDate = dateFormatter.date(from: orchard.plantedDate) ?? Date()
                rowWidth = String(orchard.rowWidth)
                rowWidthUnit = orchard.rowWidthLinearUnit
                distanceBetweenTrees = String(orchard.distanceBetweenTrees)
                distanceUnit = orchard.distanceBetweenTreesLinearUnit
            }
            observeOrchards()
        }
    }

    private func observeOrchards() {
        let flow = orchardViewModel.allOrchards as! CommonStateFlow<NSArray>
        flow.subscribe { list in
            DispatchQueue.main.async {
                self.orchards = (list as? [Orchard]) ?? []
                if self.waitingForSave && !self.orchards.isEmpty {
                    self.waitingForSave = false
                    self.onComplete()
                }
            }
        }
    }

    private func saveOrchard() {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        let plantedDateStr = dateFormatter.string(from: plantedDate)
        let orchard = Orchard(
            id: existingOrchard?.id ?? 0,
            persistentId: existingOrchard?.persistentId ?? UUID().uuidString,
            farmId: farmId,
            crop: crop,
            plantedDate: plantedDateStr,
            rowWidth: Double(rowWidth) ?? 0.0,
            rowWidthLinearUnit: rowWidthUnit,
            distanceBetweenTrees: Double(distanceBetweenTrees) ?? 0.0,
            distanceBetweenTreesLinearUnit: distanceUnit,
            sand: 0.0, silt: 0.0, clay: 0.0, organicMatter: 0.0,
            validFrom: existingOrchard?.validFrom ?? Date().toKotlinInstant(),
            validTo: nil
        )
        if orchard.id > 0 {
            orchardViewModel.updateOrchard(orchard: orchard)
            onComplete()
        } else {
            orchardViewModel.addOrchard(orchard: orchard)
            waitingForSave = true
        }
    }
}
