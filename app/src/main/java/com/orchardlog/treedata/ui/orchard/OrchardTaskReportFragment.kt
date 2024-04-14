package com.orchardlog.treedata.ui.orchard

import android.content.Context
import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.orchardlog.treedata.R
import com.orchardlog.treedata.data.DateConverter
import com.orchardlog.treedata.databinding.FragmentOrchardTaskReportBinding
import com.orchardlog.treedata.utils.DatePickerFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.Calendar

@AndroidEntryPoint
class OrchardTaskReportFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var _binging: FragmentOrchardTaskReportBinding? = null
    private val binding get() = _binging
    private var farmOrchardsMap: Map<Long, String>? = null
    private var orchardId: Long = 0L
    private val calStart = Calendar.getInstance()
    private val yearStart = calStart.get(Calendar.YEAR)
    private var startDate = LocalDate.of(yearStart, 1, 1)
    private var endDate = LocalDate.of(yearStart, 12, 31)
    private var isStartDateValid = false
    private var isEndDateValid = false
    private val orchardViewModel: OrchardViewModel by viewModels()
    private val orchardActivityReport = "OrchardActivityReport.pdf"
    private val orchardActivityDateKey = "orchardActivityDate"
    private val orchardActivityFromDateRequestKey = "requestOrchardActivityFromDateKey"
    private val orchardActivityToDateRequestKey = "requestOrchardActivityToDateKey"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener(orchardActivityFromDateRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.orchardTaskFromDate?.setText(bundle.getString(
            orchardActivityDateKey
        ))
            isStartDateValid = true
            if(isEndDateValid && orchardId > 0L) {
                startDate = DateConverter().toOffsetDate(bundle.getString(orchardActivityDateKey))
                getOrchardWithOrchardActivities(orchardId, startDate, endDate)
            }
        }
        childFragmentManager.setFragmentResultListener(orchardActivityToDateRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.orchardTaskToDate?.setText(bundle.getString(
            orchardActivityDateKey
        ))
            isEndDateValid = true
            if(isStartDateValid && orchardId > 0L) {
                endDate = DateConverter().toOffsetDate(bundle.getString(orchardActivityDateKey))
                getOrchardWithOrchardActivities(orchardId, startDate, endDate)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binging = FragmentOrchardTaskReportBinding.inflate(inflater, container, false)
        val vw = binding?.root
        orchardViewModel.getFarmWithOrchardsMap().observe(viewLifecycleOwner) { farmWithOrchards ->
            farmOrchardsMap = farmWithOrchards
            val adapter = ArrayAdapter<String>(
                requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, farmWithOrchards.values.toList()
            )
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.selectOrchardForReport?.adapter = adapter
            binding?.selectOrchardForReport?.onItemSelectedListener = this
        }

        binding?.taskOrchardReportBtn?.setOnClickListener {
            createPDF()
        }

        datePicker()

        return vw
    }

    private fun getOrchardWithOrchardActivities(orchardId: Long, startDate: LocalDate, endDate: LocalDate) {
        orchardViewModel.getOrchardWithOrchardActivities(orchardId, startDate, endDate).observe(
            viewLifecycleOwner
        ) { orchardsWithOrchardActivities ->
            val adapter =
                OrchardActivityExpandableAdapter(orchardsWithOrchardActivities, requireContext())
            binding?.orchardActivityListView?.setAdapter(adapter)

        }
    }

    private fun datePicker() {
        binding?.orchardTaskFromDateCal?.setOnClickListener {
            DatePickerFragment(orchardActivityFromDateRequestKey, orchardActivityDateKey)
                .show(childFragmentManager, getString(R.string.from_date))
        }

        binding?.orchardTaskToDateCal?.setOnClickListener {
            DatePickerFragment(orchardActivityToDateRequestKey, orchardActivityDateKey)
                .show(childFragmentManager, getString(R.string.to_date))
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val obj = parent?.adapter?.getItem(position)
        if(obj is String && !obj.isEmpty() && !farmOrchardsMap?.isEmpty()!!) {
            val key = this.farmOrchardsMap?.filter { it.value == obj }?.keys?.first()
            this.orchardId = key!!
            val ssDate = binding?.orchardTaskFromDate?.text.toString()
            val seDate = binding?.orchardTaskToDate?.text.toString()
            try {
                if(!ssDate.isEmpty() && !seDate.isEmpty()) {
                    startDate = DateConverter().toOffsetDate(ssDate)
                    endDate = DateConverter().toOffsetDate(seDate)
                }
                getOrchardWithOrchardActivities(orchardId, startDate, endDate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

        val page: PdfDocument.Page = document.startPage(pageInfo)

        val canvas = page.canvas
        val paint = Paint()
        val isDark = requireContext().isDarkThemeOn()
        if (isDark) {
            //paint.color = Color.WHITE
            Log.i("OrchardTaskReportFragment", "the color is white")
            paint.setARGB(255,255,255,255)
        } else {
            //paint.color = Color.BLACK
            Log.i("OrchardTaskReportFragment", "the color is white")
            paint.setARGB(0,0,0,0)
        }

        // draw something on the page
        view?.draw(canvas)

        // finish the page
        document.finishPage(page)
        // add more pages
        var outputStream: FileOutputStream? = null
        try {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val report = File(path, orchardActivityReport)
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