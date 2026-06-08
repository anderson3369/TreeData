//
//  FertilizerView.swift
//  TreeData
//
//  Created by Steven Anderson on 4/16/26.
//

import SwiftUI
import shared

struct FertilizerView: View {
    @State private var selectedTab = 0

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                Picker("", selection: $selectedTab) {
                    Text("Fertilizers").tag(0)
                    Text("Applications").tag(1)
                    Text("Report").tag(2)
                }
                .pickerStyle(.segmented)
                .padding(.horizontal)
                .padding(.top, 8)

                if selectedTab == 0 {
                    FertilizerFormView()
                } else if selectedTab == 1 {
                    FertilizerApplicationFormView()
                } else {
                    FertilizerReportView()
                }
            }
            .navigationTitle("Fertilizers")
        }
    }
}

// MARK: - Fertilizer Form

struct FertilizerFormView: View {
    private let fertilizerViewModel = ViewModelProvider.shared.fertilizerViewModel

    @State private var fertilizers: [Fertilizer] = []
    @State private var selectedFertilizer: Fertilizer? = nil

    @State private var name: String = ""
    @State private var nitrogen: String = ""
    @State private var phosphorous: String = ""
    @State private var potassium: String = ""
    @State private var sulfur: String = ""
    @State private var calcium: String = ""
    @State private var magnesium: String = ""
    @State private var iron: String = ""
    @State private var zinc: String = ""
    @State private var manganese: String = ""
    @State private var boron: String = ""
    @State private var molybdenum: String = ""
    @State private var chloride: String = ""
    @State private var copper: String = ""
    @State private var selenium: String = ""
    @State private var nickel: String = ""
    @State private var organicMatter: String = ""

    var body: some View {
        Form {
            Section(header: Text("Existing Fertilizers")) {
                Picker("Select a Fertilizer", selection: $selectedFertilizer) {
                    Text("New Fertilizer").tag(nil as Fertilizer?)
                    ForEach(fertilizers, id: \.id) { fertilizer in
                        Text(fertilizer.name).tag(fertilizer as Fertilizer?)
                    }
                }
                .onChange(of: selectedFertilizer) { newValue in
                    populateFields(from: newValue)
                }
            }

            Section(header: Text("Fertilizer Details")) {
                TextField("Name", text: $name)
            }

            Section(header: Text("Primary Nutrients (%)")) {
                TextField("Nitrogen (N)", text: $nitrogen)
                    .keyboardType(.decimalPad)
                TextField("Phosphorous (P)", text: $phosphorous)
                    .keyboardType(.decimalPad)
                TextField("Potassium (K)", text: $potassium)
                    .keyboardType(.decimalPad)
            }

            Section(header: Text("Secondary Nutrients (%)")) {
                TextField("Sulfur (S)", text: $sulfur)
                    .keyboardType(.decimalPad)
                TextField("Calcium (Ca)", text: $calcium)
                    .keyboardType(.decimalPad)
                TextField("Magnesium (Mg)", text: $magnesium)
                    .keyboardType(.decimalPad)
            }

            Section(header: Text("Micronutrients (%)")) {
                TextField("Iron (Fe)", text: $iron)
                    .keyboardType(.decimalPad)
                TextField("Zinc (Zn)", text: $zinc)
                    .keyboardType(.decimalPad)
                TextField("Manganese (Mn)", text: $manganese)
                    .keyboardType(.decimalPad)
                TextField("Boron (B)", text: $boron)
                    .keyboardType(.decimalPad)
                TextField("Molybdenum (Mo)", text: $molybdenum)
                    .keyboardType(.decimalPad)
                TextField("Chloride (Cl)", text: $chloride)
                    .keyboardType(.decimalPad)
                TextField("Copper (Cu)", text: $copper)
                    .keyboardType(.decimalPad)
                TextField("Selenium (Se)", text: $selenium)
                    .keyboardType(.decimalPad)
                TextField("Nickel (Ni)", text: $nickel)
                    .keyboardType(.decimalPad)
            }

            Section(header: Text("Other")) {
                TextField("Organic Matter (%)", text: $organicMatter)
                    .keyboardType(.decimalPad)
            }

            Section {
                HStack {
                    Button("Save") { saveFertilizer() }
                        .buttonStyle(.borderedProminent)
                        .tint(.orchardPrimary)

                    Spacer()

                    Button("New") { resetForm() }
                        .buttonStyle(.bordered)

                    Spacer()

                    Button("Delete", role: .destructive) { deleteFertilizer() }
                        .buttonStyle(.bordered)
                        .disabled(selectedFertilizer == nil)
                }
            }
        }
        .task {
            observeData()
        }
    }

    private func observeData() {
        let flow = fertilizerViewModel.fertilizers as! CommonStateFlow<NSArray>
        flow.subscribe { list in
            DispatchQueue.main.async {
                self.fertilizers = (list as? [Fertilizer]) ?? []
            }
        }
    }

    private func populateFields(from fertilizer: Fertilizer?) {
        if let f = fertilizer {
            name = f.name
            nitrogen = String(f.nitrogen)
            phosphorous = String(f.phosphorous)
            potassium = String(f.potassium)
            sulfur = String(f.sulfur)
            calcium = String(f.calcium)
            magnesium = String(f.magnesium)
            iron = String(f.iron)
            zinc = String(f.zinc)
            manganese = String(f.manganese)
            boron = String(f.boron)
            molybdenum = String(f.molybdenum)
            chloride = String(f.chloride)
            copper = String(f.copper)
            selenium = String(f.selenium)
            nickel = String(f.nickel)
            organicMatter = String(f.organicMatter)
        } else {
            resetForm()
        }
    }

    private func resetForm() {
        selectedFertilizer = nil
        name = ""
        nitrogen = ""
        phosphorous = ""
        potassium = ""
        sulfur = ""
        calcium = ""
        magnesium = ""
        iron = ""
        zinc = ""
        manganese = ""
        boron = ""
        molybdenum = ""
        chloride = ""
        copper = ""
        selenium = ""
        nickel = ""
        organicMatter = ""
    }

    private func saveFertilizer() {
        guard !name.isEmpty else { return }

        let fertilizer = Fertilizer(
            id: selectedFertilizer?.id ?? 0,
            name: name,
            nitrogen: Double(nitrogen) ?? 0.0,
            phosphorous: Double(phosphorous) ?? 0.0,
            potassium: Double(potassium) ?? 0.0,
            sulfur: Double(sulfur) ?? 0.0,
            calcium: Double(calcium) ?? 0.0,
            magnesium: Double(magnesium) ?? 0.0,
            iron: Double(iron) ?? 0.0,
            zinc: Double(zinc) ?? 0.0,
            manganese: Double(manganese) ?? 0.0,
            boron: Double(boron) ?? 0.0,
            molybdenum: Double(molybdenum) ?? 0.0,
            chloride: Double(chloride) ?? 0.0,
            copper: Double(copper) ?? 0.0,
            selenium: Double(selenium) ?? 0.0,
            nickel: Double(nickel) ?? 0.0,
            organicMatter: Double(organicMatter) ?? 0.0,
            firestoreId: selectedFertilizer?.firestoreId ?? UUID().uuidString
        )

        if fertilizer.id > 0 {
            fertilizerViewModel.updateFertilizer(fertilizer: fertilizer)
        } else {
            fertilizerViewModel.addFertilizer(fertilizer: fertilizer)
        }
        resetForm()
    }

    private func deleteFertilizer() {
        if let fertilizer = selectedFertilizer {
            fertilizerViewModel.deleteFertilizer(fertilizer: fertilizer)
            resetForm()
        }
    }
}

// MARK: - Fertilizer Application Form

struct FertilizerApplicationFormView: View {
    private let fertilizerViewModel = ViewModelProvider.shared.fertilizerViewModel
    private let orchardViewModel = ViewModelProvider.shared.orchardViewModel

    @State private var fertilizers: [Fertilizer] = []
    @State private var orchards: [OrchardWithFarm] = []
    @State private var fertilizerApplications: [FertilizerApplicationWithItems] = []
    @State private var selectedApplication: FertilizerApplicationWithItems? = nil
    @State private var currentItems: [FertilizerApplicationItem] = []

    // Application fields
    @State private var selectedOrchardId: Int64 = 0
    @State private var applicationStart: Date = Date()
    @State private var applicationStop: Date = Date()
    @State private var areaTreated: String = ""
    @State private var orchardUnit: OrchardUnit = .acre

    // Application Item fields (Temporary state for the item being edited)
    @State private var selectedFertilizerId: Int64 = 0
    @State private var itemApplied: String = ""
    @State private var itemAppliedUnit: WeightOrMeasureUnit = .pounds

    private let weightUnits: [WeightOrMeasureUnit] = [
        .pounds, .tons, .ounces, .grams, .gallons, .quarts, .pints, .fluidounces
    ]
    private let orchardUnits: [OrchardUnit] = [.acre, .squarefeet, .hectare]

    var body: some View {
        Form {
            Section(header: Text("Existing Applications")) {
                Picker("Select an Application", selection: $selectedApplication) {
                    Text("New Application").tag(nil as FertilizerApplicationWithItems?)
                    ForEach(fertilizerApplications, id: \.application.id) { app in
                        Text(app.application.description_()).tag(app as FertilizerApplicationWithItems?)
                    }
                }
                .onChange(of: selectedApplication) { newValue in
                    populateFields(from: newValue)
                }
            }

            Section(header: Text("Application Details")) {
                Picker("Orchard", selection: $selectedOrchardId) {
                    if orchards.isEmpty {
                        Text("No Orchards").tag(Int64(0))
                    }
                    ForEach(orchards, id: \.orchard.id) { orchardWithFarm in
                        Text(orchardWithFarm.description()).tag(orchardWithFarm.orchard.id)
                    }
                }

                DatePicker("Start", selection: $applicationStart)
                DatePicker("Stop", selection: $applicationStop)

                HStack {
                    TextField("Area Treated", text: $areaTreated)
                        .keyboardType(.decimalPad)
                    Picker("Unit", selection: $orchardUnit) {
                        ForEach(orchardUnits, id: \.self) { unit in
                            Text(unit.description()).tag(unit)
                        }
                    }
                    .pickerStyle(.menu)
                }
            }

            Section(header: Text("Fertilizers in this Application")) {
                ForEach(currentItems, id: \.fertilizerId) { item in
                    HStack {
                        Text(fertilizerName(for: item.fertilizerId))
                        Spacer()
                        Text("\(String(format: "%.2f", item.applied)) \(item.appliedUnit.description())")
                        Button(action: { removeItem(item) }) {
                            Image(systemName: "minus.circle.fill")
                                .foregroundColor(.red)
                        }
                        .buttonStyle(.borderless)
                    }
                }

                Divider()

                VStack(alignment: .leading) {
                    Text("Add Fertilizer").font(.caption).foregroundColor(.gray)
                    Picker("Fertilizer", selection: $selectedFertilizerId) {
                        if fertilizers.isEmpty {
                            Text("No Fertilizers").tag(Int64(0))
                        }
                        ForEach(fertilizers, id: \.id) { fertilizer in
                            Text(fertilizer.name).tag(fertilizer.id)
                        }
                    }

                    HStack {
                        TextField("Amount", text: $itemApplied)
                            .keyboardType(.decimalPad)
                        Picker("Unit", selection: $itemAppliedUnit) {
                            ForEach(weightUnits, id: \.self) { unit in
                                Text(unit.description()).tag(unit)
                            }
                        }
                        .pickerStyle(.menu)

                        Button(action: addItem) {
                            Image(systemName: "plus.circle.fill")
                                .foregroundColor(.orchardPrimary)
                                .font(.title2)
                        }
                        .buttonStyle(.borderless)
                        .disabled(itemApplied.isEmpty || selectedFertilizerId == 0)
                    }
                }
            }

            Section {
                HStack {
                    Button("Save Application") { saveApplication() }
                        .buttonStyle(.borderedProminent)
                        .tint(.orchardPrimary)
                        .disabled(currentItems.isEmpty)

                    Spacer()

                    Button("New") { resetForm() }
                        .buttonStyle(.bordered)

                    Spacer()

                    Button("Delete", role: .destructive) { deleteApplication() }
                        .buttonStyle(.bordered)
                        .disabled(selectedApplication == nil)
                }
            }
        }
        .task {
            observeData()
        }
    }

    private func fertilizerName(for id: Int64) -> String {
        fertilizers.first(where: { $0.id == id })?.name ?? "Unknown"
    }

    private func addItem() {
        guard let amount = Double(itemApplied), selectedFertilizerId != 0 else { return }

        // Check if already exists, update if so
        if let index = currentItems.firstIndex(where: { $0.fertilizerId == selectedFertilizerId }) {
            currentItems[index] = FertilizerApplicationItem(
                fertilizerApplicationId: selectedApplication?.application.id ?? 0,
                fertilizerId: selectedFertilizerId,
                applied: amount,
                appliedUnit: itemAppliedUnit
            )
        } else {
            currentItems.append(FertilizerApplicationItem(
                fertilizerApplicationId: selectedApplication?.application.id ?? 0,
                fertilizerId: selectedFertilizerId,
                applied: amount,
                appliedUnit: itemAppliedUnit
            ))
        }

        // Reset item fields
        itemApplied = ""
    }

    private func removeItem(_ item: FertilizerApplicationItem) {
        currentItems.removeAll(where: { $0.fertilizerId == item.fertilizerId })
    }

    private func observeData() {
        let fertilizersFlow = fertilizerViewModel.fertilizers as! CommonStateFlow<NSArray>
        fertilizersFlow.subscribe { list in
            DispatchQueue.main.async {
                let fList = (list as? [Fertilizer]) ?? []
                self.fertilizers = fList
                if selectedFertilizerId == 0, let first = fList.first {
                    selectedFertilizerId = first.id
                }
            }
        }

        let applicationsFlow = fertilizerViewModel.fertilizerApplicationsWithItems as! CommonStateFlow<NSArray>
        applicationsFlow.subscribe { list in
            DispatchQueue.main.async {
                self.fertilizerApplications = (list as? [FertilizerApplicationWithItems]) ?? []
            }
        }

        let orchardsFlow = orchardViewModel.orchardsWithFarm as! CommonStateFlow<NSArray>
        orchardsFlow.subscribe { list in
            DispatchQueue.main.async {
                let oList = (list as? [OrchardWithFarm]) ?? []
                self.orchards = oList
                if selectedOrchardId == 0, let first = oList.first {
                    selectedOrchardId = first.orchard.id
                }
            }
        }
    }

    private func populateFields(from applicationWithItems: FertilizerApplicationWithItems?) {
        if let appWithItems = applicationWithItems {
            let app = appWithItems.application
            selectedOrchardId = app.orchardId
            applicationStart = app.applicationStart.toSwiftDate()
            applicationStop = app.applicationStop.toSwiftDate()
            areaTreated = String(app.areaTreated)
            orchardUnit = app.orchardUnit

            currentItems = appWithItems.items
        } else {
            resetForm()
        }
    }

    private func resetForm() {
        selectedApplication = nil
        applicationStart = Date()
        applicationStop = Date()
        areaTreated = ""
        orchardUnit = .acre
        currentItems = []
        itemApplied = ""
    }

    private func saveApplication() {
        guard selectedOrchardId != 0, !currentItems.isEmpty else { return }

        let application = FertilizerApplication(
            id: selectedApplication?.application.id ?? 0,
            orchardId: selectedOrchardId,
            applicationStart: applicationStart.toKotlinInstant(),
            applicationStop: applicationStop.toKotlinInstant(),
            areaTreated: Double(areaTreated) ?? 0.0,
            orchardUnit: orchardUnit,
            firestoreId: selectedApplication?.application.firestoreId ?? UUID().uuidString
        )

        if application.id > 0 {
            fertilizerViewModel.updateFertilizerApplicationWithItems(
                application: application,
                items: currentItems
            )
        } else {
            fertilizerViewModel.saveFertilizerApplicationWithItems(
                application: application,
                items: currentItems
            )
        }
        resetForm()
    }

    private func deleteApplication() {
        if let appWithItems = selectedApplication {
            fertilizerViewModel.deleteFertilizerApplicationWithItems(
                application: appWithItems.application
            )
            resetForm()
        }
    }
}

// MARK: - Fertilizer Report

struct FertilizerReportView: View {
    private let fertilizerViewModel = ViewModelProvider.shared.fertilizerViewModel
    private let orchardViewModel = ViewModelProvider.shared.orchardViewModel

    @State private var orchards: [OrchardWithFarm] = []
    @State private var selectedOrchardId: Int64 = 0
    @State private var startDate = Calendar.current.date(from: DateComponents(year: Calendar.current.component(.year, from: Date()), month: 1, day: 1))!
    @State private var endDate = Calendar.current.date(from: DateComponents(year: Calendar.current.component(.year, from: Date()), month: 12, day: 31))!
    @State private var reportData: [FertilizerApplicationWithFertilizers] = []

    private var startInstant: KotlinInstant {
        startDate.toKotlinInstant()
    }

    private var endInstant: KotlinInstant {
        // End of day for the end date
        let endOfDay = Calendar.current.date(bySettingHour: 23, minute: 59, second: 59, of: endDate) ?? endDate
        return endOfDay.toKotlinInstant()
    }

    var body: some View {
        Form {
            Section(header: Text("Report Filters")) {
                Picker("Orchard", selection: $selectedOrchardId) {
                    Text("Select an Orchard").tag(Int64(0))
                    ForEach(orchards, id: \.orchard.id) { orchardWithFarm in
                        Text(orchardWithFarm.description()).tag(orchardWithFarm.orchard.id)
                    }
                }

                DatePicker("From", selection: $startDate, displayedComponents: .date)
                DatePicker("To", selection: $endDate, displayedComponents: .date)

                Button("Generate Report") {
                    loadReport()
                }
                .buttonStyle(.borderedProminent)
                .tint(.orchardPrimary)
            }

            if !reportData.isEmpty {
                ForEach(reportData, id: \.fertilizerApplication.id) { appWithFert in
                    Section(header: Text(appWithFert.fertilizerApplication.description_())
                        .foregroundColor(.white)
                        .font(.subheadline.weight(.bold))
                    ) {
                        ForEach(appWithFert.fertilizers, id: \.id) { fertilizer in
                            Text(fertilizer.name)
                                .padding(.leading, 8)
                        }
                    }
                    .listRowBackground(Color.orchardPrimary.opacity(0.8))
                }
            } else if selectedOrchardId != 0 {
                Section {
                    Text("No fertilizer applications found for the selected filters.")
                        .foregroundColor(.gray)
                }
            }
        }
        .task {
            observeOrchards()
        }
    }

    private func observeOrchards() {
        let orchardsFlow = orchardViewModel.orchardsWithFarm as! CommonStateFlow<NSArray>
        orchardsFlow.subscribe { list in
            DispatchQueue.main.async {
                let oList = (list as? [OrchardWithFarm]) ?? []
                self.orchards = oList
                if selectedOrchardId == 0, let first = oList.first {
                    selectedOrchardId = first.orchard.id
                }
            }
        }
    }

    private func loadReport() {
        guard selectedOrchardId != 0 else { return }
        let flow = fertilizerViewModel.getFertilizerApplicationsWithFertilizers(
            orchardId: selectedOrchardId,
            startDate: startInstant,
            endDate: endInstant
        ) as! CommonStateFlow<NSArray>
        flow.subscribe { list in
            DispatchQueue.main.async {
                self.reportData = (list as? [FertilizerApplicationWithFertilizers]) ?? []
            }
        }
    }
}
