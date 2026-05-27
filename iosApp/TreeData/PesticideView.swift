//
//  PesticideView.swift
//  TreeData
//
//  Created by Steven Anderson on 4/16/26.
//

import SwiftUI
import shared

struct PesticideView: View {
    @State private var selectedTab = 0

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                Picker("", selection: $selectedTab) {
                    Text("Pesticides").tag(0)
                    Text("Applications").tag(1)
                    Text("Report").tag(2)
                }
                .pickerStyle(.segmented)
                .padding(.horizontal)
                .padding(.top, 8)

                if selectedTab == 0 {
                    PesticideFormView()
                } else if selectedTab == 1 {
                    PesticideApplicationFormView()
                } else {
                    PesticideReportView()
                }
            }
            .navigationTitle("Pesticides")
        }
    }
}

// MARK: - Pesticide Form

struct PesticideFormView: View {
    private let pesticideViewModel = ViewModelProvider.shared.pesticideViewModel

    @State private var pesticides: [Pesticide] = []
    @State private var selectedPesticide: Pesticide? = nil

    // Form fields
    @State private var productName: String = ""
    @State private var eparegno: String = ""
    @State private var signalWord: SignalWord = .caution
    @State private var rei: String = ""
    @State private var reiUnit: REIUnit = .hour

    private let signalWords: [SignalWord] = [.danger, .warning, .caution]
    private let reiUnits: [REIUnit] = [.hour, .day]

    var body: some View {
        Form {
            Section(header: Text("Existing Pesticides")) {
                Picker("Select a Pesticide", selection: $selectedPesticide) {
                    Text("New Pesticide").tag(nil as Pesticide?)
                    ForEach(pesticides, id: \.id) { pesticide in
                        Text(pesticide.productName).tag(pesticide as Pesticide?)
                    }
                }
                .onChange(of: selectedPesticide) { newValue in
                    populateFields(from: newValue)
                }
            }

            Section(header: Text("Pesticide Details")) {
                TextField("Product Name", text: $productName)
                TextField("EPA Reg. No.", text: $eparegno)
            }

            Section(header: Text("Safety Information")) {
                Picker("Signal Word", selection: $signalWord) {
                    ForEach(signalWords, id: \.self) { word in
                        Text(word.description()).tag(word)
                    }
                }

                HStack {
                    TextField("REI", text: $rei)
                        .keyboardType(.numberPad)
                    Picker("Unit", selection: $reiUnit) {
                        ForEach(reiUnits, id: \.self) { unit in
                            Text(unit.description()).tag(unit)
                        }
                    }
                    .pickerStyle(.menu)
                }
            }

            Section {
                HStack {
                    Button("Save") { savePesticide() }
                        .buttonStyle(.borderedProminent)
                        .tint(.orchardPrimary)

                    Spacer()

                    Button("New") { resetForm() }
                        .buttonStyle(.bordered)

                    Spacer()

                    Button("Delete", role: .destructive) { deletePesticide() }
                        .buttonStyle(.bordered)
                        .disabled(selectedPesticide == nil)
                }
            }
        }
        .task {
            observeData()
        }
    }

    private func observeData() {
        let flow = pesticideViewModel.pesticides as! CommonStateFlow<NSArray>
        flow.subscribe { list in
            DispatchQueue.main.async {
                self.pesticides = (list as? [Pesticide]) ?? []
            }
        }
    }

    private func populateFields(from pesticide: Pesticide?) {
        if let p = pesticide {
            productName = p.productName
            eparegno = p.eparegno
            signalWord = p.signalWord
            rei = String(p.rei)
            reiUnit = p.reiUnit
        } else {
            resetForm()
        }
    }

    private func resetForm() {
        selectedPesticide = nil
        productName = ""
        eparegno = ""
        signalWord = .caution
        rei = ""
        reiUnit = .hour
    }

    private func savePesticide() {
        guard !productName.isEmpty else { return }

        let pesticide = Pesticide(
            id: selectedPesticide?.id ?? 0,
            productName: productName,
            eparegno: eparegno,
            signalWord: signalWord,
            rei: Int32(rei) ?? 0,
            reiUnit: reiUnit
        )

        if pesticide.id > 0 {
            pesticideViewModel.updatePesticide(pesticide: pesticide)
        } else {
            pesticideViewModel.addPesticide(pesticide: pesticide)
        }
        resetForm()
    }

    private func deletePesticide() {
        if let pesticide = selectedPesticide {
            pesticideViewModel.deletePesticide(pesticide: pesticide)
            resetForm()
        }
    }
}

// MARK: - Pesticide Application Form

struct PesticideApplicationFormView: View {
    private let pesticideViewModel = ViewModelProvider.shared.pesticideViewModel
    private let orchardViewModel = ViewModelProvider.shared.orchardViewModel

    @State private var pesticides: [Pesticide] = []
    @State private var orchards: [OrchardWithFarm] = []
    @State private var pesticideApplications: [PesticideApplicationWithItems] = []
    @State private var selectedApplication: PesticideApplicationWithItems? = nil
    @State private var currentItems: [PesticideApplicationItem] = []

    // Application fields
    @State private var selectedOrchardId: Int64 = 0
    @State private var applicationStart: Date = Date()
    @State private var applicationStop: Date = Date()
    @State private var areaTreated: String = ""
    @State private var areaTreatedUnit: OrchardUnit = .acre
    @State private var dilution: String = ""
    @State private var dilutionUnit: WeightOrMeasureUnit = .gallons
    @State private var applicationMethod: ApplicationMethod = .air

    // Application Item fields (Temporary state for the item being edited)
    @State private var selectedPesticideId: Int64 = 0
    @State private var itemApplied: String = ""
    @State private var itemAppliedUnit: WeightOrMeasureUnit = .pounds

    private let weightUnits: [WeightOrMeasureUnit] = [
        .pounds, .tons, .ounces, .grams, .gallons, .quarts, .pints, .fluidounces
    ]
    private let orchardUnits: [OrchardUnit] = [.acre, .squarefeet, .hectare]
    private let applicationMethods: [ApplicationMethod] = [.air, .airblast, .chemigation, .hand]

    var body: some View {
        Form {
            Section(header: Text("Existing Applications")) {
                Picker("Select an Application", selection: $selectedApplication) {
                    Text("New Application").tag(nil as PesticideApplicationWithItems?)
                    ForEach(pesticideApplications, id: \.application.id) { app in
                        Text(app.application.description_()).tag(app as PesticideApplicationWithItems?)
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
                    Picker("Unit", selection: $areaTreatedUnit) {
                        ForEach(orchardUnits, id: \.self) { unit in
                            Text(unit.description()).tag(unit)
                        }
                    }
                    .pickerStyle(.menu)
                }

                HStack {
                    TextField("Dilution", text: $dilution)
                        .keyboardType(.numberPad)
                    Picker("Unit", selection: $dilutionUnit) {
                        ForEach(weightUnits, id: \.self) { unit in
                            Text(unit.description()).tag(unit)
                        }
                    }
                    .pickerStyle(.menu)
                }

                Picker("Method", selection: $applicationMethod) {
                    ForEach(applicationMethods, id: \.self) { method in
                        Text(method.description()).tag(method)
                    }
                }
            }

            Section(header: HStack {
                Text("Pesticides in this Application")
                Spacer()
                Button("Clear All") {
                    currentItems = []
                }
                .font(.caption)
                .textCase(.none)
                .foregroundColor(.red)
                .disabled(currentItems.isEmpty)
            }) {
                ForEach(currentItems, id: \.pesticideId) { item in
                    HStack {
                        VStack(alignment: .leading) {
                            Text(pesticideName(for: item.pesticideId))
                                .font(.headline)
                            Text("\(String(format: "%.2f", item.applied)) \(item.appliedUnit.description())")
                                .font(.subheadline)
                                .foregroundColor(.gray)
                        }
                        Spacer()
                        Button(action: { editItem(item) }) {
                            Image(systemName: "pencil.circle.fill")
                                .foregroundColor(.orchardSecondary)
                                .font(.title2)
                        }
                        .buttonStyle(.borderless)
                        Button(action: { removeItem(item) }) {
                            Image(systemName: "minus.circle.fill")
                                .foregroundColor(.red)
                                .font(.title2)
                        }
                        .buttonStyle(.borderless)
                    }
                    .padding(.vertical, 4)
                }

                Divider()

                VStack(alignment: .leading) {
                    Text(currentItems.contains(where: { $0.pesticideId == selectedPesticideId }) ? "Update Pesticide" : "Add Pesticide")
                        .font(.caption)
                        .foregroundColor(.gray)

                    Picker("Pesticide", selection: $selectedPesticideId) {
                        if pesticides.isEmpty {
                            Text("No Pesticides").tag(Int64(0))
                        }
                        ForEach(pesticides, id: \.id) { pesticide in
                            Text(pesticide.productName).tag(pesticide.id)
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
                            Image(systemName: currentItems.contains(where: { $0.pesticideId == selectedPesticideId }) ? "arrow.up.circle.fill" : "plus.circle.fill")
                                .foregroundColor(currentItems.contains(where: { $0.pesticideId == selectedPesticideId }) ? .orchardSecondary : .orchardPrimary)
                                .font(.title2)
                        }
                        .buttonStyle(.borderless)
                        .disabled(itemApplied.isEmpty || selectedPesticideId == 0)
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

    private func pesticideName(for id: Int64) -> String {
        pesticides.first(where: { $0.id == id })?.productName ?? "Unknown"
    }

    private func addItem() {
        guard let amount = Double(itemApplied), selectedPesticideId != 0 else { return }

        let newItem = PesticideApplicationItem(
            pesticideApplicationId: selectedApplication?.application.id ?? 0,
            pesticideId: selectedPesticideId,
            applied: amount,
            appliedUnit: itemAppliedUnit
        )

        if let index = currentItems.firstIndex(where: { $0.pesticideId == selectedPesticideId }) {
            currentItems[index] = newItem
        } else {
            currentItems.append(newItem)
        }
        itemApplied = ""
    }

    private func removeItem(_ item: PesticideApplicationItem) {
        currentItems.removeAll(where: { $0.pesticideId == item.pesticideId })
    }

    private func editItem(_ item: PesticideApplicationItem) {
        selectedPesticideId = item.pesticideId
        itemApplied = String(item.applied)
        itemAppliedUnit = item.appliedUnit
    }

    private func observeData() {
        let pesticidesFlow = pesticideViewModel.pesticides as! CommonStateFlow<NSArray>
        pesticidesFlow.subscribe { list in
            DispatchQueue.main.async {
                let pList = (list as? [Pesticide]) ?? []
                self.pesticides = pList
                if selectedPesticideId == 0, let first = pList.first {
                    selectedPesticideId = first.id
                }
            }
        }

        let applicationsFlow = pesticideViewModel.pesticideApplicationsWithItems as! CommonStateFlow<NSArray>
        applicationsFlow.subscribe { list in
            DispatchQueue.main.async {
                self.pesticideApplications = (list as? [PesticideApplicationWithItems]) ?? []
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

    private func populateFields(from applicationWithItems: PesticideApplicationWithItems?) {
        if let appWithItems = applicationWithItems {
            let app = appWithItems.application
            selectedOrchardId = app.orchardId
            applicationStart = app.applicationStart.toSwiftDate()
            applicationStop = app.applicationStop.toSwiftDate()
            areaTreated = String(app.areaTreated)
            areaTreatedUnit = app.areaTreatedUnit
            dilution = String(app.dilution)
            dilutionUnit = app.dilutionUnit
            applicationMethod = app.applicationMethod

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
        areaTreatedUnit = .acre
        dilution = ""
        dilutionUnit = .gallons
        applicationMethod = .air
        currentItems = []
        itemApplied = ""
    }

    private func saveApplication() {
        guard selectedOrchardId != 0, !currentItems.isEmpty else { return }

        let application = PesticideApplication(
            id: selectedApplication?.application.id ?? 0,
            orchardId: selectedOrchardId,
            applicationStart: applicationStart.toKotlinInstant(),
            applicationStop: applicationStop.toKotlinInstant(),
            dilution: Int32(dilution) ?? 0,
            dilutionUnit: dilutionUnit,
            areaTreated: Double(areaTreated) ?? 0.0,
            areaTreatedUnit: areaTreatedUnit,
            applicationMethod: applicationMethod
        )

        if application.id > 0 {
            pesticideViewModel.updatePesticideApplicationWithItems(
                application: application,
                items: currentItems
            )
        } else {
            pesticideViewModel.savePesticideApplicationWithItems(
                application: application,
                items: currentItems
            )
        }
        resetForm()
    }

    private func deleteApplication() {
        if let appWithItems = selectedApplication {
            pesticideViewModel.deletePesticideApplicationWithItems(
                application: appWithItems.application
            )
            resetForm()
        }
    }
}

// MARK: - Pesticide Report

struct PesticideReportView: View {
    private let pesticideViewModel = ViewModelProvider.shared.pesticideViewModel
    private let orchardViewModel = ViewModelProvider.shared.orchardViewModel

    @State private var orchards: [OrchardWithFarm] = []
    @State private var selectedOrchardId: Int64 = 0
    @State private var startDate = Calendar.current.date(from: DateComponents(year: Calendar.current.component(.year, from: Date()), month: 1, day: 1))!
    @State private var endDate = Calendar.current.date(from: DateComponents(year: Calendar.current.component(.year, from: Date()), month: 12, day: 31))!
    @State private var reportData: [PesticideApplicationWithPesticides] = []

    private var startInstant: KotlinInstant {
        startDate.toKotlinInstant()
    }

    private var endInstant: KotlinInstant {
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
                ForEach(reportData, id: \.pesticideApplication.id) { appWithPest in
                    Section(header: Text(appWithPest.pesticideApplication.description_())
                        .foregroundColor(.white)
                        .font(.subheadline.weight(.bold))
                    ) {
                        ForEach(appWithPest.pesticides, id: \.id) { pesticide in
                            let item = appWithPest.items.first(where: { $0.pesticideId == pesticide.id })
                            let dosage = item != nil ? " (\(String(format: "%.2f", item!.applied)) \(item!.appliedUnit.description()))" : ""
                            Text("\(pesticide.productName)\(dosage)")
                                .padding(.leading, 8)
                        }
                    }
                    .listRowBackground(Color.orchardPrimary.opacity(0.8))
                }
            } else if selectedOrchardId != 0 {
                Section {
                    Text("No pesticide applications found for the selected filters.")
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
        let flow = pesticideViewModel.getPesticideApplicationWithPesticides(
            orchardId: selectedOrchardId,
            startDate: startInstant,
            endDate: endInstant
        ) as! CommonStateFlow<NSArray>
        flow.subscribe { list in
            DispatchQueue.main.async {
                self.reportData = (list as? [PesticideApplicationWithPesticides]) ?? []
            }
        }
    }
}
