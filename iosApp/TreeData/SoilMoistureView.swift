//
//  SoilMoistureView.swift
//  TreeData
//
//  Created by Steven Anderson on 4/16/26.
//

import SwiftUI
import shared

struct SoilMoistureView: View {
    private let irrigationViewModel = ViewModelProvider.shared.irrigationViewModel
    private let orchardViewModel = ViewModelProvider.shared.orchardViewModel

    @State private var soilMoistures: [SoilMoisture] = []
    @State private var orchards: [OrchardWithFarm] = []
    @State private var selectedSoilMoisture: SoilMoisture? = nil

    // Form fields
    @State private var selectedOrchardId: Int64 = 0
    @State private var readingDate: Date = Date()
    @State private var centibar: String = ""
    @State private var percent: String = ""

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Existing Readings")) {
                    Picker("Select a Reading", selection: $selectedSoilMoisture) {
                        Text("New Reading").tag(nil as SoilMoisture?)
                        ForEach(soilMoistures, id: \.id) { reading in
                            Text(readingLabel(reading)).tag(reading as SoilMoisture?)
                        }
                    }
                    .onChange(of: selectedSoilMoisture) { newValue in
                        populateFields(from: newValue)
                    }
                }

                Section(header: Text("Moisture Details")) {
                    Picker("Orchard", selection: $selectedOrchardId) {
                        if orchards.isEmpty {
                            Text("No Orchards").tag(Int64(0))
                        }
                        ForEach(orchards, id: \.orchard.id) { orchardWithFarm in
                            Text(orchardWithFarm.description()).tag(orchardWithFarm.orchard.id)
                        }
                    }

                    DatePicker("Reading Date", selection: $readingDate)

                    HStack {
                        Text("Centibar")
                        Spacer()
                        TextField("0-200", text: $centibar)
                            .keyboardType(.numberPad)
                            .multilineTextAlignment(.trailing)
                    }

                    HStack {
                        Text("Percentage")
                        Spacer()
                        TextField("0-100", text: $percent)
                            .keyboardType(.numberPad)
                            .multilineTextAlignment(.trailing)
                    }
                }

                Section {
                    HStack {
                        Button("Save") { saveSoilMoisture() }
                            .buttonStyle(.borderedProminent)
                            .tint(.orchardPrimary)

                        Spacer()

                        Button("New") { resetForm() }
                            .buttonStyle(.bordered)

                        Spacer()

                        Button("Delete", role: .destructive) { deleteSoilMoisture() }
                            .buttonStyle(.bordered)
                            .disabled(selectedSoilMoisture == nil)
                    }
                }
            }
            .navigationTitle("Soil Moisture")
            .task {
                observeData()
            }
        }
    }

    private func readingLabel(_ reading: SoilMoisture) -> String {
        let orchardWithFarm = orchards.first(where: { $0.orchard.id == reading.orchardId })
        let orchardInfo = orchardWithFarm?.description_() ?? "Unknown Orchard"
        let dateStr = reading.date.toSwiftDate().formatted(date: .abbreviated, time: .shortened)
        return "\(orchardInfo) - \(dateStr)"
    }

    private func observeData() {
        let moistureFlow = irrigationViewModel.soilMoisture as! CommonStateFlow<NSArray>
        moistureFlow.subscribe { list in
            DispatchQueue.main.async {
                self.soilMoistures = (list as? [SoilMoisture]) ?? []
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

    private func populateFields(from reading: SoilMoisture?) {
        if let r = reading {
            selectedOrchardId = r.orchardId
            readingDate = r.date.toSwiftDate()
            centibar = String(r.centibar)
            percent = String(r.percent)
        } else {
            resetForm()
        }
    }

    private func resetForm() {
        selectedSoilMoisture = nil
        readingDate = Date()
        centibar = ""
        percent = ""
    }

    private func saveSoilMoisture() {
        guard selectedOrchardId != 0 else { return }

        let reading = SoilMoisture(
            id: selectedSoilMoisture?.id ?? 0,
            orchardId: selectedOrchardId,
            date: readingDate.toKotlinInstant(),
            centibar: Int32(centibar) ?? 0,
            percent: Int32(percent) ?? 0
        )

        if reading.id > 0 {
            irrigationViewModel.updateSoilMoisture(soilMoisture: reading)
        } else {
            irrigationViewModel.addSoilMoisture(soilMoisture: reading)
        }
        resetForm()
    }

    private func deleteSoilMoisture() {
        if let reading = selectedSoilMoisture {
            irrigationViewModel.deleteSoilMoisture(soilMoisture: reading)
            resetForm()
        }
    }
}
