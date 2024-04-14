package com.orchardlog.treedata.ui.fertilizer

import android.content.Context
import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.orchardlog.treedata.R
import com.orchardlog.treedata.data.DateConverter
import com.orchardlog.treedata.data.Validator
import com.orchardlog.treedata.databinding.FragmentFertilizerReportBinding
import com.orchardlog.treedata.ui.orchard.OrchardViewModel
import com.orchardlog.treedata.utils.DatePickerFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.Calendar

@AndroidEntryPoint
class FertilizerReportFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val fertilizerViewModel: FertilizerViewModel by viewModels()
    private val orchardViewModel: OrchardViewModel by viewModels()
    private var _binding: FragmentFertilizerReportBinding? = null
    private val binding get() = _binding
    private var farmOrchardsMap: Map<Long, String>? = null
    private val calStart = Calendar.getInstance()
    private val yearStart = calStart.get(Calendar.YEAR)
    private var startDate = LocalDate.of(yearStart, 1, 1)
    private var endDate = LocalDate.of(yearStart, 12, 31)

    private var isStartDateValid = false
    private var isEndDateValid = false
    private var orchardId: Long = 0L

    companion object {
        const val fertilizerReport = "FertilizerReport.pdf"
        const val fertilizerDateKey = "fertilizerDate"
        const val fertilizerFromDateRequestKey = "requestFertilizerFromDateKey"
        const val fertilizerToDateRequestKey = "requestFertilizerToDateKey"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFertilizerReportBinding.inflate(inflater, container, false)
        val vw = binding?.root
        orchardViewModel.getFarmWithOrchardsMap().observe(viewLifecycleOwner) {
                farmWithOrchards ->
            farmOrchardsMap = farmWithOrchards
            val adapter = ArrayAdapter<String>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, farmWithOrchards.values.toList())
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.fertilizerOrchardSpinner?.adapter = adapter
            binding?.fertilizerOrchardSpinner?.onItemSelectedListener = this
        }

        datePicker()

        binding?.fertilizerFromDate?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val date = binding?.fertilizerFromDate?.text.toString()
                if(!Validator.validateDate(date)) {
                    Toast.makeText(requireContext(), "Invalid date format mm-dd-yyyy", Toast.LENGTH_LONG).show()
                } else {
                    isStartDateValid = true
                    if(isEndDateValid && orchardId > 0L) {
                        startDate = DateConverter().toOffsetDate(date)
                        getFertilizerApplicationWithFertilizers()
                    }
                }
            }
        })

        binding?.fertilizerToDate?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val date = binding?.fertilizerToDate?.text.toString()
                if(!Validator.validateDate(date)) {
                    Toast.makeText(requireContext(), "Invalid date format mm-dd-yyyy", Toast.LENGTH_LONG).show()
                } else {
                    isEndDateValid = true
                    if(isStartDateValid && orchardId > 0L) {
                        endDate = DateConverter().toOffsetDate(date)
                        getFertilizerApplicationWithFertilizers()
                    }
                }
            }
        })

        binding?.fertilizerReportBtn?.setOnClickListener {
            createPDF()
        }

        return vw
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener(fertilizerFromDateRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.fertilizerFromDate?.setText(bundle.getString(fertilizerDateKey))
            isStartDateValid = true
            if(isEndDateValid && orchardId > 0L) {
                startDate = DateConverter().toOffsetDate(bundle.getString(fertilizerDateKey))
                getFertilizerApplicationWithFertilizers()
            }
        }
        childFragmentManager.setFragmentResultListener(fertilizerToDateRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.fertilizerToDate?.setText(bundle.getString(fertilizerDateKey))
            isEndDateValid = true
            if(isStartDateValid && orchardId > 0L) {
                endDate = DateConverter().toOffsetDate(bundle.getString(fertilizerDateKey))
                getFertilizerApplicationWithFertilizers()
            }
        }
    }

    private fun datePicker() {
        binding?.fertilizerFromDateCal?.setOnClickListener {
            DatePickerFragment(fertilizerFromDateRequestKey, fertilizerDateKey)
                .show(childFragmentManager, getString(R.string.from_date))
        }

        binding?.fertilizerToDateCal?.setOnClickListener {
            DatePickerFragment(fertilizerToDateRequestKey, fertilizerDateKey)
                .show(childFragmentManager, getString(R.string.to_date))
        }
    }

    private fun getFertilizerApplicationWithFertilizers() {
        fertilizerViewModel.getFertilizerApplicationsWithFertilizers(orchardId, startDate, endDate)
            .observe(viewLifecycleOwner) {
                    fertilizerApplicationsWithFertilizers ->
                val adapter = FertilizerExpandableAdapter(fertilizerApplicationsWithFertilizers, requireContext())
                binding?.fertilizerListView?.setAdapter(adapter)
            }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val obj = parent?.adapter?.getItem(position)
        if(obj is String && !obj.isEmpty() && !farmOrchardsMap?.isEmpty()!!) {
            val key = this.farmOrchardsMap?.filter { it.value == obj }?.keys?.first()
            this.orchardId = key!!
            val ssDate = binding?.fertilizerFromDate?.text.toString()
            val seDate = binding?.fertilizerToDate?.text.toString()
            try {
                if(!ssDate.isEmpty() && !seDate.isEmpty()) {
                    startDate = DateConverter().toOffsetDate(ssDate)
                    endDate = DateConverter().toOffsetDate(seDate)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            getFertilizerApplicationWithFertilizers()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    fun Context.isDarkThemeOn(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private fun createPDF() {
        val document = PdfDocument()

        // create a page description
        val pageInfo = PdfDocument.PageInfo.Builder(576, 792, 1).create()

        // start a page
        //x,y start 72
        // start a page
        val page: PdfDocument.Page = document.startPage(pageInfo)
        //page.canvas.

        // draw something on the page
        val canvas = page.canvas
        val paint = Paint()
        val isDark = requireContext().isDarkThemeOn()
        if (isDark) {
            //paint.color = Color.WHITE
            //Log.i("IrrigationReportFragment", "the color is white")
            paint.setARGB(255,255,255,255)
        } else {
            //paint.color = Color.BLACK
            //Log.i("IrrigationReportFragment", "the color is white")
            paint.setARGB(0,0,0,0)
        }
        //paint.style = Paint.Style.FILL
        //paint.typeface = Typeface.DEFAULT
        //canvas.drawPaint(paint)
        //paint.textSize = 14f
        //Log.i("IrrigationReportFragment", "the paint color is " + paint.color.toString())

        // draw something on the page
        view?.draw(canvas)

        // finish the page
        document.finishPage(page)
        // add more pages
        var outputStream: FileOutputStream? = null
        try {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val report = File(path, fertilizerReport)
            outputStream = FileOutputStream(report.path)
            // write the document content
            document.writeTo(outputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream?.flush()
            outputStream?.close()
            // close the document
            document.close()
        }
    }


}