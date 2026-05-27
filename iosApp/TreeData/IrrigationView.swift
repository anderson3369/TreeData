//
//  IrrigationView.swift
//  TreeData
//
//  Created by Steven Anderson on 4/16/26.
//

import SwiftUI
import shared

struct IrrigationView: View {
    @State private var selectedTab = 0

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                Picker("", selection: $selectedTab) {
                    Text("Systems").tag(0)
                    Text("Irrigations").tag(1)
                    Text("Report").tag(2)
                }
                .pickerStyle(.segmented)
                .padding(.horizontal)
                .padding(.top, 8)

                if selectedTab == 0 {
                    IrrigationSystemFormView()
                } else if selectedTab == 1 {
                    IrrigationEventFormView()
                } else {
                    IrrigationReportView()
                }
            }
            .navigationTitle("Irrigation")
        }
    }
}

// MARK: - Irrigation System Form

struct IrrigationSystemFormView: View {
    private let irrigationViewModel = ViewModelProvider.shared.irrigationViewModel
    private let orchardViewModel = ViewModelProvider.shared.orchardViewModel
    private let pumpViewModel = ViewModelProvider.shared.pumpViewModel

    @State private var irrigationSystems: [IrrigationSystem] = []
    @State private var orchards: [OrchardWithFarm] = []
    @State private var pumps: [Pump] = []
    @State private var selectedSystem: IrrigationSystem? = nil

    // Form fields
    @State private var selectedOrchardId: Int64 = 0
    @State private var selectedPumpId: Int64 = 0
    @State private var name: String = ""
    @State private var irrigationMethod: IrrigationMethod = .drip
    @State private var emitterFlowRate: String = ""
    @State private var emitterFlowUnit: FlowRateUnit = .gallonsperhour
    @State private var emitterRadius: String = ""
    @State private var emitterRadiusUnit: LinearUnit = .feet
    @State private var emitterSpacing: String = ""
    @State private var emitterSpacingUnit: LinearUnit = .feet

    private let irrigationMethods: [IrrigationMethod] = [.sprinkler, .microsprinkler, .drip, .flood]
    private let flowRateUnits: [FlowRateUnit] = [.gallonsperhour, .gallonsperminute]
    private let linearUnits: [LinearUnit] = [.feet, .inches, .meters]

    var body: some View {
        Form {
            Section(header: Text("Existing Irrigation Systems")) {
                Picker("Select a System", selection: $selectedSystem) {
                    Text("New System").tag(nil as IrrigationSystem?)
                    ForEach(irrigationSystems, id: \.id) { system in
                        Text(system.name).tag(system as IrrigationSystem?)
                    }
                }
                .onChange(of: selectedSystem) { newValue in
                    populateFields(from: newValue)
                }
            }

            Section(header: Text("System Details")) {
                TextField("Name", text: $name)

                Picker("Orchard", selection: $selectedOrchardId) {
                    if orchards.isEmpty {
                        Text("No Orchards").tag(Int64(0))
                    }
                    ForEach(orchards, id: \.orchard.id) { orchardWithFarm in
                        Text(orchardWithFarm.description()).tag(orchardWithFarm.orchard.id)
                    }
                }

                Picker("Pump", selection: $selectedPumpId) {
                    if pumps.isEmpty {
                        Text("No Pumps").tag(Int64(0))
                    }
                    ForEach(pumps, id: \.id) { pump in
                        Text(pump.type).tag(pump.id)
                    }
                }

                Picker("Irrigation Method", selection: $irrigationMethod) {
                    ForEach(irrigationMethods, id: \.self) { method in
                        Text(method.description()).tag(method)
                    }
                }
            }

            Section(header: Text("Emitter Details")) {
                HStack {
                    TextField("Flow Rate", text: $emitterFlowRate)
                        .keyboardType(.decimalPad)
                    Picker("Unit", selection: $emitterFlowUnit) {
                        ForEach(flowRateUnits, id: \.self) { unit in
                            Text(unit.description()).tag(unit)
                        }
                    }
                    .pickerStyle(.menu)
                }

                HStack {
                    TextField("Radius", text: $emitterRadius)
                        .keyboardType(.decimalPad)
                    Picker("Unit", selection: $emitterRadiusUnit) {
                        ForEach(linearUnits, id: \.self) { unit in
                            Text(unit.description()).tag(unit)
                        }
                    }
                    .pickerStyle(.menu)
                }

                HStack {
                    TextField("Spacing", text: $emitterSpacing)
                        .keyboardType(.decimalPad)
                    Picker("Unit", selection: $emitterSpacingUnit) {
                        ForEach(linearUnits, id: \.self) { unit in
                            Text(unit.description()).tag(unit)
                        }
                    }
                    .pickerStyle(.menu)
                }
            }

            Section {
                HStack {
                    Button("Save") { saveSystem() }
                        .buttonStyle(.borderedProminent)
                        .tint(.orchardPrimary)

                    Spacer()

                    Button("New") { resetForm() }
                        .buttonStyle(.bordered)

                    Spacer()

                    Button("Delete", role: .destructive) { deleteSystem() }
                        .buttonStyle(.bordered)
                        .disabled(selectedSystem == nil)
                }
            }
        }
        .task {
            observeData()
        }
    }

    private func observeData() {
        let systemsFlow = irrigationViewModel.irrigationSystems as! CommonStateFlow<NSArray>
        systemsFlow.subscribe { list in
            DispatchQueue.main.async {
                self.irrigationSystems = (list as? [IrrigationSystem]) ?? []
            }
        }

        let orchardsFlow = orchardViewModel.orchardsWithFarm as! CommonStateFlow<NSArray>
        orchardsFlow.subscribe { list in
            DispatchQueue.main.async {
                let orchardList = (list as? [OrchardWithFarm]) ?? []
                self.orchards = orchardList
                if selectedOrchardId == 0, let first = orchardList.first {
                    selectedOrchardId = first.orchard.id
                }
            }
        }

        let pumpsFlow = pumpViewModel.pumps as! CommonStateFlow<NSArray>
        pumpsFlow.subscribe { list in
            DispatchQueue.main.async {
                let pumpList = (list as? [Pump]) ?? []
                self.pumps = pumpList
                if selectedPumpId == 0, let first = pumpList.first {
                    selectedPumpId = first.id
                }
            }
        }
    }

    private func populateFields(from system: IrrigationSystem?) {
        if let s = system {
            selectedOrchardId = s.orchardId
            selectedPumpId = s.pumpId
            name = s.name
            irrigationMethod = s.irrigationMethod
            emitterFlowRate = String(s.emitterFlowRate)
            emitterFlowUnit = s.emitterFlowUnit
            emitterRadius = String(s.emitterRadius)
            emitterRadiusUnit = s.emitterRadiusLinearUnit
            emitterSpacing = String(s.emitterSpacing)
            emitterSpacingUnit = s.emitterSpacingLinearUnit
        } else {
            resetForm()
        }
    }

    private func resetForm() {
        selectedSystem = nil
        name = ""
        irrigationMethod = .drip
        emitterFlowRate = ""
        emitterFlowUnit = .gallonsperhour
        emitterRadius = ""
        emitterRadiusUnit = .feet
        emitterSpacing = ""
        emitterSpacingUnit = .feet
    }

    private func saveSystem() {
        guard !name.isEmpty, selectedOrchardId != 0, selectedPumpId != 0 else { return }

        let system = IrrigationSystem(
            id: selectedSystem?.id ?? 0,
            orchardId: selectedOrchardId,
            pumpId: selectedPumpId,
            name: name,
            irrigationMethod: irrigationMethod,
            emitterFlowRate: Double(emitterFlowRate) ?? 0.0,
            emitterFlowUnit: emitterFlowUnit,
            emitterRadius: Double(emitterRadius) ?? 0.0,
            emitterRadiusLinearUnit: emitterRadiusUnit,
            emitterSpacing: Double(emitterSpacing) ?? 0.0,
            emitterSpacingLinearUnit: emitterSpacingUnit
        )

        if system.id > 0 {
            irrigationViewModel.updateIrrigationSystem(irrigationSystem: system)
        } else {
            irrigationViewModel.addIrrigationSystem(irrigationSystem: system)
        }
        resetForm()
    }

    private func deleteSystem() {
        if let system = selectedSystem {
            irrigationViewModel.deleteIrrigationSystem(irrigationSystem: system)
            resetForm()
        }
    }
}

// MARK: - Irrigation Event Form

struct IrrigationEventFormView: View {
    private let irrigationViewModel = ViewModelProvider.shared.irrigationViewModel

    @State private var irrigations: [Irrigation] = []
    @State private var irrigationSystems: [IrrigationSystem] = []
    @State private var selectedIrrigation: Irrigation? = nil

    // Form fields
    @State private var selectedSystemId: Int64 = 0
    @State private var startTime: Date = Date()
    @State private var stopTime: Date = Date()

    var body: some View {
        Form {
            Section(header: Text("Existing Irrigations")) {
                Picker("Select an Irrigation", selection: $selectedIrrigation) {
                    Text("New Irrigation").tag(nil as Irrigation?)
                    ForEach(irrigations, id: \.id) { irrigation in
                        Text(irrigationLabel(irrigation)).tag(irrigation as Irrigation?)
                    }
                }
                .onChange(of: selectedIrrigation) { newValue in
                    populateFields(from: newValue)
                }
            }

            Section(header: Text("Irrigation Details")) {
                Picker("Irrigation System", selection: $selectedSystemId) {
                    if irrigationSystems.isEmpty {
                        Text("No Systems").tag(Int64(0))
                    }
                    ForEach(irrigationSystems, id: \.id) { system in
                        Text(system.name).tag(system.id)
                    }
                }

                DatePicker("Start", selection: $startTime)
                DatePicker("Stop", selection: $stopTime)
            }

            Section {
                HStack {
                    Button("Save") { saveIrrigation() }
                        .buttonStyle(.borderedProminent)
                        .tint(.orchardPrimary)

                    Spacer()

                    Button("New") { resetForm() }
                        .buttonStyle(.bordered)

                    Spacer()

                    Button("Delete", role: .destructive) { deleteIrrigation() }
                        .buttonStyle(.bordered)
                        .disabled(selectedIrrigation == nil)
                }
            }
        }
        .task {
            observeData()
        }
    }

    private func irrigationLabel(_ irrigation: Irrigation) -> String {
        let systemName = irrigationSystems.first(where: { $0.id == irrigation.irrigationSystemId })?.name ?? "System"
        let dateStr = irrigation.startTime.toSwiftDate().formatted(date: .abbreviated, time: .shortened)
        return "\(systemName) - \(dateStr)"
    }

    private func observeData() {
        let irrigationsFlow = irrigationViewModel.irrigations as! CommonStateFlow<NSArray>
        irrigationsFlow.subscribe { list in
            DispatchQueue.main.async {
                self.irrigations = (list as? [Irrigation]) ?? []
            }
        }

        let systemsFlow = irrigationViewModel.irrigationSystems as! CommonStateFlow<NSArray>
        systemsFlow.subscribe { list in
            DispatchQueue.main.async {
                let systemList = (list as? [IrrigationSystem]) ?? []
                self.irrigationSystems = systemList
                if selectedSystemId == 0, let first = systemList.first {
                    selectedSystemId = first.id
                }
            }
        }
    }

    private func populateFields(from irrigation: Irrigation?) {
        if let i = irrigation {
            selectedSystemId = i.irrigationSystemId
            startTime = i.startTime.toSwiftDate()
            stopTime = i.stopTime.toSwiftDate()
        } else {
            resetForm()
        }
    }

    private func resetForm() {
        selectedIrrigation = nil
        startTime = Date()
        stopTime = Date()
    }

    private func saveIrrigation() {
        guard selectedSystemId != 0 else { return }

        let irrigation = Irrigation(
            id: selectedIrrigation?.id ?? 0,
            irrigationSystemId: selectedSystemId,
            startTime: startTime.toKotlinInstant(),
            stopTime: stopTime.toKotlinInstant()
        )

        if irrigation.id > 0 {
            irrigationViewModel.updateIrrigation(irrigation: irrigation)
        } else {
            irrigationViewModel.addIrrigation(irrigation: irrigation)
        }
        resetForm()
    }

    private func deleteIrrigation() {
        if let irrigation = selectedIrrigation {
            irrigationViewModel.deleteIrrigation(irrigation: irrigation)
            resetForm()
        }
    }
}

// MARK: - Irrigation Report

struct IrrigationReportView: View {
    private let irrigationViewModel = ViewModelProvider.shared.irrigationViewModel
    private let orchardViewModel = ViewModelProvider.shared.orchardViewModel
    private let pumpViewModel = ViewModelProvider.shared.pumpViewModel

    @State private var orchards: [OrchardWithFarm] = []
    @State private var selectedOrchardId: Int64 = 0
    @State private var startDate = Calendar.current.date(from: DateComponents(year: Calendar.current.component(.year, from: Date()), month: 1, day: 1))!
    @State private var endDate = Calendar.current.date(from: DateComponents(year: Calendar.current.component(.year, from: Date()), month: 12, day: 31))!

    @State private var irrigations: [Irrigation] = []
    @State private var irrigationSystems: [IrrigationSystem] = []
    @State private var pumps: [Pump] = []
    @State private var hasGenerated = false

    private var filteredIrrigations: [Irrigation] {
        let systemIds = Set(irrigationSystems.filter { $0.orchardId == selectedOrchardId }.map { $0.id })
        return irrigations.filter { irrigation in
            guard systemIds.contains(irrigation.irrigationSystemId) else { return false }
            let irrigDate = irrigation.startTime.toSwiftDate()
            return irrigDate >= startDate && irrigDate <= endDate
        }
    }

    private var totalHours: Int64 {
        var total: Int64 = 0
        for irrigation in filteredIrrigations {
            let start = irrigation.startTime.epochSeconds
            let stop = irrigation.stopTime.epochSeconds
            total += (stop - start) / 3600
        }
        return total
    }

    private var totalGallons: Double {
        let systemIds = Set(irrigationSystems.filter { $0.orchardId == selectedOrchardId }.map { $0.id })
        let pumpIds = Set(irrigationSystems.filter { systemIds.contains($0.id) }.map { $0.pumpId })
        guard let pump = pumps.first(where: { pumpIds.contains($0.id) }) else { return 0.0 }
        let flowRate = pump.flowRate
        let isPerMinute = pump.flowRateUnit == .gallonsperminute
        return isPerMinute ? flowRate * 60.0 * Double(totalHours) : flowRate * Double(totalHours)
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
                    hasGenerated = true
                }
                .buttonStyle(.borderedProminent)
                .tint(.orchardPrimary)
            }

            if hasGenerated && selectedOrchardId != 0 {
                Section(header: Text("Results")) {
                    HStack {
                        Text("Total Irrigation Hours:")
                        Spacer()
                        Text("\(totalHours)")
                            .fontWeight(.bold)
                    }

                    HStack {
                        Text("Total Gallons Pumped:")
                        Spacer()
                        Text(String(format: "%.0f", totalGallons))
                            .fontWeight(.bold)
                    }
                }
            }
        }
        .task {
            observeData()
        }
    }

    private func observeData() {
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

        let irrigationsFlow = irrigationViewModel.irrigations as! CommonStateFlow<NSArray>
        irrigationsFlow.subscribe { list in
            DispatchQueue.main.async {
                self.irrigations = (list as? [Irrigation]) ?? []
            }
        }

        let systemsFlow = irrigationViewModel.irrigationSystems as! CommonStateFlow<NSArray>
        systemsFlow.subscribe { list in
            DispatchQueue.main.async {
                self.irrigationSystems = (list as? [IrrigationSystem]) ?? []
            }
        }

        let pumpsFlow = pumpViewModel.pumps as! CommonStateFlow<NSArray>
        pumpsFlow.subscribe { list in
            DispatchQueue.main.async {
                self.pumps = (list as? [Pump]) ?? []
            }
        }
    }
}
