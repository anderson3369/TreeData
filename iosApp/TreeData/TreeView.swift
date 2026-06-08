//
//  TreeView.swift
//  TreeData
//
//  Created by Steven Anderson on 4/16/26.
//

import SwiftUI
import MapKit
import CoreLocation
import shared

struct TreeView: View {
    @State private var selectedTab = 0
    // Shared state for map-to-form communication
    @State private var treeToEdit: Tree? = nil
    @State private var newTreeLatitude: String = ""
    @State private var newTreeLongitude: String = ""

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                Picker("", selection: $selectedTab) {
                    Text("Trees").tag(0)
                    Text("Map").tag(1)
                }
                .pickerStyle(.segmented)
                .padding(.horizontal)
                .padding(.top, 8)

                if selectedTab == 0 {
                    TreeFormView(
                        treeToEdit: $treeToEdit,
                        newTreeLatitude: $newTreeLatitude,
                        newTreeLongitude: $newTreeLongitude
                    )
                } else {
                    TreeMapView(
                        onSelectTree: { tree in
                            treeToEdit = tree
                            selectedTab = 0
                        },
                        onPlaceNewTree: { coordinate in
                            treeToEdit = nil
                            newTreeLatitude = String(coordinate.latitude)
                            newTreeLongitude = String(coordinate.longitude)
                            selectedTab = 0
                        }
                    )
                }
            }
            .navigationTitle("Trees")
        }
    }
}

// MARK: - Tree Map View

// MARK: - Location Manager for high-accuracy GPS

class TreeLocationManager: NSObject, ObservableObject, CLLocationManagerDelegate {
    private let manager = CLLocationManager()
    @Published var lastLocation: CLLocation? = nil
    @Published var isAcquiring = false
    @Published var accuracyMessage: String = ""

    private var onLocationAcquired: ((CLLocationCoordinate2D) -> Void)? = nil

    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
    }

    func requestLocation(completion: @escaping (CLLocationCoordinate2D) -> Void) {
        onLocationAcquired = completion
        isAcquiring = true
        accuracyMessage = "Acquiring GPS fix..."
        manager.requestWhenInUseAuthorization()
        manager.startUpdatingLocation()
    }

    func cancel() {
        manager.stopUpdatingLocation()
        isAcquiring = false
        onLocationAcquired = nil
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }
        lastLocation = location
        let accuracy = location.horizontalAccuracy

        if accuracy <= 5.0 {
            // Good enough accuracy — use this fix
            accuracyMessage = String(format: "Accuracy: %.1f m", accuracy)
            manager.stopUpdatingLocation()
            isAcquiring = false
            onLocationAcquired?(location.coordinate)
            onLocationAcquired = nil
        } else {
            accuracyMessage = String(format: "Accuracy: %.1f m (refining...)", accuracy)
        }
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        accuracyMessage = "Location error: \(error.localizedDescription)"
        isAcquiring = false
    }

    /// Accept the current best location even if accuracy > 5m
    func acceptCurrentLocation() {
        if let location = lastLocation {
            manager.stopUpdatingLocation()
            isAcquiring = false
            onLocationAcquired?(location.coordinate)
            onLocationAcquired = nil
        }
    }
}

struct TreeMapView: View {
    private let treeViewModel = ViewModelProvider.shared.treeViewModel

    var onSelectTree: (Tree) -> Void
    var onPlaceNewTree: (CLLocationCoordinate2D) -> Void

    @State private var trees: [Tree] = []
    @State private var rootstocks: [Rootstock] = []
    @State private var varieties: [Variety] = []
    @State private var cameraPosition: MapCameraPosition = .userLocation(fallback: .automatic)
    @State private var currentCamera: MapCamera? = nil
    @StateObject private var locationManager = TreeLocationManager()

    var body: some View {
        ZStack {
            Map(position: $cameraPosition) {
                UserAnnotation()

                ForEach(trees, id: \.id) { tree in
                    if tree.latitude != 0.0 || tree.longitude != 0.0 {
                        Annotation(
                            varietyName(for: tree.varietyId),
                            coordinate: CLLocationCoordinate2D(
                                latitude: tree.latitude,
                                longitude: tree.longitude
                            )
                        ) {
                            Button {
                                onSelectTree(tree)
                            } label: {
                                Image(systemName: "tree.fill")
                                    .font(.title2)
                                    .foregroundColor(rankingColor(tree.treeRanking))
                                    .padding(6)
                                    .background(Circle().fill(.white).shadow(radius: 2))
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }
            }
            .mapStyle(.hybrid)
            .mapControls {
                MapUserLocationButton()
                MapCompass()
                MapScaleView()
            }
            .onMapCameraChange { context in
                currentCamera = context.camera
            }

            // Overlay controls
            VStack {
                Spacer()

                // GPS acquisition banner
                if locationManager.isAcquiring {
                    VStack(spacing: 8) {
                        ProgressView()
                        Text(locationManager.accuracyMessage)
                            .font(.subheadline.bold())
                        HStack(spacing: 16) {
                            if locationManager.lastLocation != nil {
                                Button("Use Current Fix") {
                                    locationManager.acceptCurrentLocation()
                                }
                                .buttonStyle(.borderedProminent)
                                .tint(.orchardPrimary)
                            }
                            Button("Cancel") {
                                locationManager.cancel()
                            }
                            .buttonStyle(.bordered)
                        }
                    }
                    .padding()
                    .background(.ultraThinMaterial)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.horizontal)
                    .padding(.bottom, 8)
                }

                HStack {
                    // Zoom controls
                    VStack(spacing: 8) {
                        Button {
                            zoomIn()
                        } label: {
                            Image(systemName: "plus")
                                .font(.title3.bold())
                                .frame(width: 44, height: 44)
                                .background(.regularMaterial)
                                .clipShape(RoundedRectangle(cornerRadius: 8))
                        }

                        Button {
                            zoomOut()
                        } label: {
                            Image(systemName: "minus")
                                .font(.title3.bold())
                                .frame(width: 44, height: 44)
                                .background(.regularMaterial)
                                .clipShape(RoundedRectangle(cornerRadius: 8))
                        }
                    }
                    .padding(.leading)

                    Spacer()

                    // New tree button
                    if !locationManager.isAcquiring {
                        Button {
                            locationManager.requestLocation { coordinate in
                                onPlaceNewTree(coordinate)
                            }
                        } label: {
                            Label("New Tree", systemImage: "plus.circle.fill")
                                .font(.subheadline.bold())
                                .padding(.horizontal, 12)
                                .padding(.vertical, 10)
                                .background(Color.orchardPrimary)
                                .foregroundColor(.white)
                                .clipShape(Capsule())
                        }
                        .padding(.trailing)
                    }
                }
                .padding(.bottom, 16)
            }
        }
        .task {
            observeData()
        }
    }

    private func zoomIn() {
        zoom(by: 0.5)
    }

    private func zoomOut() {
        zoom(by: 2.0)
    }

    private func zoom(by factor: Double) {
        guard let camera = currentCamera else { return }
        let newCamera = MapCamera(
            centerCoordinate: camera.centerCoordinate,
            distance: camera.distance * factor,
            heading: camera.heading,
            pitch: camera.pitch
        )
        withAnimation {
            cameraPosition = .camera(newCamera)
        }
    }

    private func observeData() {
        let treesFlow = treeViewModel.allTrees as! CommonStateFlow<NSArray>
        treesFlow.subscribe { list in
            DispatchQueue.main.async {
                self.trees = (list as? [Tree]) ?? []
            }
        }

        let rootstocksFlow = treeViewModel.rootstocks as! CommonStateFlow<NSArray>
        rootstocksFlow.subscribe { list in
            DispatchQueue.main.async {
                self.rootstocks = (list as? [Rootstock]) ?? []
            }
        }

        let varietiesFlow = treeViewModel.varieties as! CommonStateFlow<NSArray>
        varietiesFlow.subscribe { list in
            DispatchQueue.main.async {
                self.varieties = (list as? [Variety]) ?? []
            }
        }
    }

    private func varietyName(for id: Int64) -> String {
        varieties.first(where: { $0.id == id })?.name ?? "Unknown Variety"
    }

    private func rankingColor(_ ranking: TreeRanking) -> Color {
        switch ranking {
        case .excellent: return .green
        case .good: return .blue
        case .moderate: return .yellow
        case .poor: return .orange
        case .dying: return .red
        default: return .gray
        }
    }
}

// MARK: - Tree Form

struct TreeFormView: View {
    private let treeViewModel = ViewModelProvider.shared.treeViewModel
    private let orchardViewModel = ViewModelProvider.shared.orchardViewModel

    @Binding var treeToEdit: Tree?
    @Binding var newTreeLatitude: String
    @Binding var newTreeLongitude: String

    @State private var trees: [Tree] = []
    @State private var orchards: [OrchardWithFarm] = []
    @State private var rootstocks: [Rootstock] = []
    @State private var varieties: [Variety] = []
    @State private var selectedTree: Tree? = nil

    // Form fields
    @State private var selectedOrchardId: Int64 = 0
    @State private var selectedRootstockId: Int64 = 0
    @State private var selectedVarietyId: Int64 = 0
    @State private var plantedDate: Date = Date()
    @State private var treeRanking: TreeRanking = .good
    @State private var notes: String = ""
    @State private var latitude: String = ""
    @State private var longitude: String = ""

    // For adding new rootstock/variety
    @State private var showAddRootstock = false
    @State private var showAddVariety = false
    @State private var newRootstockName: String = ""
    @State private var newRootstockCultivar: String = ""
    @State private var newRootstockType: RootstockType = .bareroot
    @State private var newVarietyName: String = ""
    @State private var newVarietyCultivar: String = ""

    private let rankings: [TreeRanking] = [.excellent, .good, .moderate, .poor, .dying]
    private let rootstockTypes: [RootstockType] = [.bareroot, .potted]

    var body: some View {
        Form {
            Section(header: Text("Existing Trees")) {
                Picker("Select a Tree", selection: $selectedTree) {
                    Text("New Tree").tag(nil as Tree?)
                    ForEach(trees, id: \.id) { tree in
                        Text("Tree #\(tree.id) - \(tree.treeRanking.description())").tag(tree as Tree?)
                    }
                }
                .onChange(of: selectedTree) { newValue in
                    populateFields(from: newValue)
                }
            }

            Section(header: Text("Tree Details")) {
                Picker("Orchard", selection: $selectedOrchardId) {
                    if orchards.isEmpty {
                        Text("No Orchards Found").tag(Int64(0))
                    }
                    ForEach(orchards, id: \.orchard.id) { orchardWithFarm in
                        Text(orchardWithFarm.description()).tag(orchardWithFarm.orchard.id)
                    }
                }

                DatePicker("Planted Date", selection: $plantedDate, displayedComponents: .date)

                Picker("Tree Ranking", selection: $treeRanking) {
                    ForEach(rankings, id: \.self) { ranking in
                        Text(ranking.description()).tag(ranking)
                    }
                }

                TextField("Notes", text: $notes, axis: .vertical)
                    .lineLimit(3...6)
            }

            Section(header: Text("Rootstock")) {
                Picker("Rootstock", selection: $selectedRootstockId) {
                    if rootstocks.isEmpty {
                        Text("No Rootstocks").tag(Int64(0))
                    }
                    ForEach(rootstocks, id: \.id) { rootstock in
                        Text(rootstock.name).tag(rootstock.id)
                    }
                }
                Button("Add Rootstock") {
                    showAddRootstock.toggle()
                }
                if showAddRootstock {
                    TextField("Name", text: $newRootstockName)
                    TextField("Cultivar", text: $newRootstockCultivar)
                    Picker("Type", selection: $newRootstockType) {
                        ForEach(rootstockTypes, id: \.self) { type in
                            Text(type.description()).tag(type)
                        }
                    }
                    Button("Save Rootstock") {
                        saveRootstock()
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(.orchardSecondary)
                }
            }

            Section(header: Text("Variety")) {
                Picker("Variety", selection: $selectedVarietyId) {
                    if varieties.isEmpty {
                        Text("No Varieties").tag(Int64(0))
                    }
                    ForEach(varieties, id: \.id) { variety in
                        Text(variety.name).tag(variety.id)
                    }
                }
                Button("Add Variety") {
                    showAddVariety.toggle()
                }
                if showAddVariety {
                    TextField("Name", text: $newVarietyName)
                    TextField("Cultivar", text: $newVarietyCultivar)
                    Button("Save Variety") {
                        saveVariety()
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(.orchardSecondary)
                }
            }

            Section(header: Text("Location")) {
                TextField("Latitude", text: $latitude)
                    .keyboardType(.decimalPad)
                TextField("Longitude", text: $longitude)
                    .keyboardType(.decimalPad)
            }

            Section {
                HStack {
                    Button("Save") { saveTree() }
                        .buttonStyle(.borderedProminent)
                        .tint(.orchardPrimary)

                    Spacer()

                    Button("New") { resetForm() }
                        .buttonStyle(.bordered)

                    Spacer()

                    Button("Delete", role: .destructive) { deleteTree() }
                        .buttonStyle(.bordered)
                        .disabled(selectedTree == nil)
                }
            }
        }
        .task {
            observeData()
        }
        .onAppear {
            applyMapData()
        }
        .onChange(of: treeToEdit) { _ in
            applyMapData()
        }
        .onChange(of: newTreeLatitude) { _ in
            applyMapData()
        }
    }

    private func observeData() {
        let treesFlow = treeViewModel.allTrees as! CommonStateFlow<NSArray>
        treesFlow.subscribe { list in
            DispatchQueue.main.async {
                self.trees = (list as? [Tree]) ?? []
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

        let rootstocksFlow = treeViewModel.rootstocks as! CommonStateFlow<NSArray>
        rootstocksFlow.subscribe { list in
            DispatchQueue.main.async {
                let rsList = (list as? [Rootstock]) ?? []
                self.rootstocks = rsList
                if selectedRootstockId == 0, let first = rsList.first {
                    selectedRootstockId = first.id
                }
            }
        }

        let varietiesFlow = treeViewModel.varieties as! CommonStateFlow<NSArray>
        varietiesFlow.subscribe { list in
            DispatchQueue.main.async {
                let vList = (list as? [Variety]) ?? []
                self.varieties = vList
                if selectedVarietyId == 0, let first = vList.first {
                    selectedVarietyId = first.id
                }
            }
        }
    }

    private func applyMapData() {
        if let tree = treeToEdit {
            selectedTree = tree
            populateFields(from: tree)
            treeToEdit = nil
        } else if !newTreeLatitude.isEmpty {
            resetForm()
            latitude = newTreeLatitude
            longitude = newTreeLongitude
            newTreeLatitude = ""
            newTreeLongitude = ""
        }
    }

    private func populateFields(from tree: Tree?) {
        if let tree = tree {
            selectedOrchardId = tree.orchardId
            selectedRootstockId = tree.rootstockId
            selectedVarietyId = tree.varietyId
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "yyyy-MM-dd"
            plantedDate = dateFormatter.date(from: tree.plantedDate) ?? Date()
            treeRanking = tree.treeRanking
            notes = tree.notes
            latitude = String(tree.latitude)
            longitude = String(tree.longitude)
        } else {
            resetForm()
        }
    }

    private func resetForm() {
        selectedTree = nil
        plantedDate = Date()
        treeRanking = .good
        notes = ""
        latitude = ""
        longitude = ""
    }

    private func saveTree() {
        guard selectedOrchardId != 0, selectedRootstockId != 0, selectedVarietyId != 0 else { return }

        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        let plantedDateStr = dateFormatter.string(from: plantedDate)
        let tree = Tree(
            id: selectedTree?.id ?? 0,
            orchardId: selectedOrchardId,
            rootstockId: selectedRootstockId,
            varietyId: selectedVarietyId,
            plantedDate: plantedDateStr,
            treeRanking: treeRanking,
            notes: notes,
            latitude: Double(latitude) ?? 0.0,
            longitude: Double(longitude) ?? 0.0,
            persistentId: selectedTree?.persistentId ?? UUID().uuidString,
            validFrom: selectedTree?.validFrom ?? Date().toKotlinInstant(),
            validTo: nil
        )

        if tree.id > 0 {
            treeViewModel.updateTree(tree: tree)
        } else {
            treeViewModel.addTree(tree: tree)
        }
        resetForm()
    }

    private func deleteTree() {
        if let tree = selectedTree {
            treeViewModel.deleteTree(tree: tree)
            resetForm()
        }
    }

    private func saveRootstock() {
        guard !newRootstockName.isEmpty else { return }
        let rootstock = Rootstock(
            id: 0,
            name: newRootstockName,
            cultivar: newRootstockCultivar,
            rootstockType: newRootstockType,
            firestoreId: UUID().uuidString
        )
        treeViewModel.addRootstock(rootstock: rootstock)
        newRootstockName = ""
        newRootstockCultivar = ""
        showAddRootstock = false
    }

    private func saveVariety() {
        guard !newVarietyName.isEmpty else { return }
        let variety = Variety(
            id: 0,
            name: newVarietyName,
            cultivar: newVarietyCultivar,
            firestoreId: UUID().uuidString
        )
        treeViewModel.addVariety(variety: variety)
        newVarietyName = ""
        newVarietyCultivar = ""
        showAddVariety = false
    }
}
