//
//  OrchardView.swift
//  TreeData
//
//  Created by Steven Anderson on 4/16/26.
//

import SwiftUI
import shared

struct OrchardView: View {
    var body: some View {
        OrchardFormView()
            .navigationTitle("Orchards")
    }
}

// MARK: - Orchard Form

struct OrchardFormView: View {
    private let orchardViewModel = ViewModelProvider.shared.orchardViewModel
    private let farmViewModel = ViewModelProvider.shared.farmViewModel

    @State private var orchards: [OrchardWithFarm] = []
    @State private var farms: [Farm] = []
    @State private var selectedOrchard: OrchardWithFarm? = nil

    // Form fields
    @State private var selectedFarmId: Int64 = 0
    @State private var crop: String = ""
    @State private var plantedDate: Date = Date()
    @State private var rowWidth: String = ""
    @State private var rowWidthUnit: LinearUnit = .feet
    @State private var distanceBetweenTrees: String = ""
    @State private var distanceBetweenTreesUnit: LinearUnit = .feet
    @State private var sand: String = ""
    @State private var silt: String = ""
    @State private var clay: String = ""
    @State private var organicMatter: String = ""

    private let linearUnits: [LinearUnit] = [.feet, .inches, .meters]

    var body: some View {
        Form {
            Section(header: Text("Existing Orchards")) {
                Picker("Select an Orchard", selection: $selectedOrchard) {
                    Text("New Orchard").tag(nil as OrchardWithFarm?)
                    ForEach(orchards, id: \.orchard.id) { orchardWithFarm in
                        Text(orchardWithFarm.description()).tag(orchardWithFarm as OrchardWithFarm?)
                    }
                }
                .onChange(of: selectedOrchard) { newValue in
                    populateFields(from: newValue)
                }
            }

            Section(header: Text("Orchard Details")) {
                Picker("Farm", selection: $selectedFarmId) {
                    if farms.isEmpty {
                        Text("No Farms Found").tag(Int64(0))
                    }
                    ForEach(farms, id: \.id) { farm in
                        Text(farm.name).tag(farm.id)
                    }
                }

                TextField("Crop", text: $crop)
                DatePicker("Planted Date", selection: $plantedDate, displayedComponents: .date)
            }

            Section(header: Text("Row Spacing")) {
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
                    TextField("Distance Between Trees", text: $distanceBetweenTrees)
                        .keyboardType(.decimalPad)
                    Picker("Unit", selection: $distanceBetweenTreesUnit) {
                        ForEach(linearUnits, id: \.self) { unit in
                            Text(unit.description()).tag(unit)
                        }
                    }
                    .pickerStyle(.menu)
                }
            }

            Section(header: Text("Soil Composition (%)")) {
                TextField("Sand", text: $sand)
                    .keyboardType(.decimalPad)
                TextField("Silt", text: $silt)
                    .keyboardType(.decimalPad)
                TextField("Clay", text: $clay)
                    .keyboardType(.decimalPad)
                TextField("Organic Matter", text: $organicMatter)
                    .keyboardType(.decimalPad)
            }

            Section {
                HStack {
                    Button("Save") { saveOrchard() }
                        .buttonStyle(.borderedProminent)
                        .tint(.orchardPrimary)

                    Spacer()

                    Button("New") { resetForm() }
                        .buttonStyle(.bordered)

                    Spacer()

                    Button("Delete", role: .destructive) { deleteOrchard() }
                        .buttonStyle(.bordered)
                        .disabled(selectedOrchard == nil)
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
                self.orchards = (list as? [OrchardWithFarm]) ?? []
            }
        }

        let farmsFlow = farmViewModel.farms as! CommonStateFlow<NSArray>
        farmsFlow.subscribe { list in
            DispatchQueue.main.async {
                let farmList = (list as? [Farm]) ?? []
                self.farms = farmList
                if selectedFarmId == 0, let first = farmList.first {
                    selectedFarmId = first.id
                }
            }
        }
    }

    private func populateFields(from orchardWithFarm: OrchardWithFarm?) {
        if let owf = orchardWithFarm {
            let orchard = owf.orchard
            selectedFarmId = orchard.farmId
            crop = orchard.crop
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "yyyy-MM-dd"
            plantedDate = dateFormatter.date(from: orchard.plantedDate) ?? Date()
            rowWidth = String(orchard.rowWidth)
            rowWidthUnit = orchard.rowWidthLinearUnit
            distanceBetweenTrees = String(orchard.distanceBetweenTrees)
            distanceBetweenTreesUnit = orchard.distanceBetweenTreesLinearUnit
            sand = String(orchard.sand)
            silt = String(orchard.silt)
            clay = String(orchard.clay)
            organicMatter = String(orchard.organicMatter)
        } else {
            resetForm()
        }
    }

    private func resetForm() {
        selectedOrchard = nil
        crop = ""
        plantedDate = Date()
        rowWidth = ""
        distanceBetweenTrees = ""
        sand = ""
        silt = ""
        clay = ""
        organicMatter = ""
        rowWidthUnit = .feet
        distanceBetweenTreesUnit = .feet
    }

    private func saveOrchard() {
        guard selectedFarmId != 0 else { return }

        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        let plantedDateStr = dateFormatter.string(from: plantedDate)
        let existingOrchard = selectedOrchard?.orchard
        let orchard = Orchard(
            id: existingOrchard?.id ?? 0,
            farmId: selectedFarmId,
            crop: crop,
            plantedDate: plantedDateStr,
            rowWidth: Double(rowWidth) ?? 0.0,
            rowWidthLinearUnit: rowWidthUnit,
            distanceBetweenTrees: Double(distanceBetweenTrees) ?? 0.0,
            distanceBetweenTreesLinearUnit: distanceBetweenTreesUnit,
            sand: Double(sand) ?? 0.0,
            silt: Double(silt) ?? 0.0,
            clay: Double(clay) ?? 0.0,
            organicMatter: Double(organicMatter) ?? 0.0,
            persistentId: existingOrchard?.persistentId ?? UUID().uuidString,
            validFrom: existingOrchard?.validFrom ?? Date().toKotlinInstant(),
            validTo: nil
        )

        if orchard.id > 0 {
            orchardViewModel.updateOrchard(orchard: orchard)
        } else {
            orchardViewModel.addOrchard(orchard: orchard)
        }
        resetForm()
    }

    private func deleteOrchard() {
        if let owf = selectedOrchard {
            orchardViewModel.deleteOrchard(orchard: owf.orchard)
            resetForm()
        }
    }
}

// MARK: - Orchard Activity Form

struct OrchardActivityFormView: View {
    private let orchardViewModel = ViewModelProvider.shared.orchardViewModel

    @State private var activities: [OrchardActivity] = []
    @State private var orchards: [OrchardWithFarm] = []
    @State private var selectedActivity: OrchardActivity? = nil

    // Form fields
    @State private var selectedOrchardId: Int64 = 0
    @State private var activity: String = "Mowing"
    @State private var notes: String = ""
    @State private var activityStart: Date = Date()
    @State private var activityStop: Date = Date()

    private let activityTypes = [
        "Mowing",
        "Pruning",
        "Discing",
        "Harvesting",
        "Equipment Maintenance",
        "Weather Event"
    ]

    var body: some View {
        Form {
            Section(header: Text("Existing Activities")) {
                Picker("Select an Activity", selection: $selectedActivity) {
                    Text("New Activity").tag(nil as OrchardActivity?)
                    ForEach(activities, id: \.id) { act in
                        Text(activityLabel(act)).tag(act as OrchardActivity?)
                    }
                }
                .onChange(of: selectedActivity) { newValue in
                    populateFields(from: newValue)
                }
            }

            Section(header: Text("Activity Details")) {
                Picker("Orchard", selection: $selectedOrchardId) {
                    if orchards.isEmpty {
                        Text("No Orchards").tag(Int64(0))
                    }
                    ForEach(orchards, id: \.orchard.id) { orchardWithFarm in
                        Text(orchardWithFarm.description()).tag(orchardWithFarm.orchard.id)
                    }
                }

                Picker("Activity", selection: $activity) {
                    ForEach(activityTypes, id: \.self) { type in
                        Text(type).tag(type)
                    }
                }

                DatePicker("Start", selection: $activityStart)
                DatePicker("Stop", selection: $activityStop)

                TextField("Notes", text: $notes, axis: .vertical)
                    .lineLimit(3...6)
            }

            Section {
                HStack {
                    Button("Save") { saveActivity() }
                        .buttonStyle(.borderedProminent)
                        .tint(.orchardPrimary)

                    Spacer()

                    Button("New") { resetForm() }
                        .buttonStyle(.bordered)

                    Spacer()

                    Button("Delete", role: .destructive) { deleteActivity() }
                        .buttonStyle(.bordered)
                        .disabled(selectedActivity == nil)
                }
            }
        }
        .task {
            observeData()
        }
    }

    private func activityLabel(_ activity: OrchardActivity) -> String {
        let dateStr = activity.activityStart.toSwiftDate().formatted(date: .abbreviated, time: .shortened)
        return "\(activity.activity) - \(dateStr)"
    }

    private func observeData() {
        let activitiesFlow = orchardViewModel.orchardActivities as! CommonStateFlow<NSArray>
        activitiesFlow.subscribe { list in
            DispatchQueue.main.async {
                self.activities = (list as? [OrchardActivity]) ?? []
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
    }

    private func populateFields(from activity: OrchardActivity?) {
        if let a = activity {
            selectedOrchardId = a.orchardId
            self.activity = a.activity
            notes = a.notes
            activityStart = a.activityStart.toSwiftDate()
            activityStop = a.activityStop.toSwiftDate()
        } else {
            resetForm()
        }
    }

    private func resetForm() {
        selectedActivity = nil
        activity = "Mowing"
        notes = ""
        activityStart = Date()
        activityStop = Date()
    }

    private func saveActivity() {
        guard selectedOrchardId != 0 else { return }

        let orchardActivity = OrchardActivity(
            id: selectedActivity?.id ?? 0,
            orchardId: selectedOrchardId,
            activity: activity,
            notes: notes,
            activityStart: activityStart.toKotlinInstant(),
            activityStop: activityStop.toKotlinInstant(),
            firestoreId: selectedActivity?.firestoreId ?? UUID().uuidString
        )

        if orchardActivity.id > 0 {
            orchardViewModel.updateOrchardActivity(orchardActivity: orchardActivity)
        } else {
            orchardViewModel.addOrchardActivity(orchardActivity: orchardActivity)
        }
        resetForm()
    }

    private func deleteActivity() {
        if let activity = selectedActivity {
            orchardViewModel.deleteOrchardActivity(orchardActivity: activity)
            resetForm()
        }
    }
}

// MARK: - Date Conversion Helpers

extension Date {
    func toKotlinInstant() -> KotlinInstant {
        let epochSeconds = Int64(self.timeIntervalSince1970)
        return KotlinInstant.companion.fromEpochSeconds(epochSeconds: epochSeconds, nanosecondAdjustment: 0)
    }
}

extension KotlinInstant {
    func toSwiftDate() -> Date {
        return Date(timeIntervalSince1970: TimeInterval(self.epochSeconds))
    }
}
