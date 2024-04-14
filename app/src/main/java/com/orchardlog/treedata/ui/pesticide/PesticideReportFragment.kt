package com.orchardlog.treedata.ui.pesticide

import android.content.Context
import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Log
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
import com.orchardlog.treedata.databinding.FragmentPesticideReportBinding
import com.orchardlog.treedata.ui.orchard.OrchardViewModel
import com.orchardlog.treedata.utils.DatePickerFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.Calendar

@AndroidEntryPoint
class PesticideReportFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val pesticideViewModel: PesticideViewModel by viewModels()
    private val orchardViewModel: OrchardViewModel by viewModels()
    private var _binding: FragmentPesticideReportBinding? = null
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
        const val pesticideReport = "PesticideReport.pdf"
        const val pesticideDateKey = "pesticideDate"
        const val pesticideFromDateRequestKey = "requestPesticideFromDateKey"
        const val pesticideToDateRequestKey = "requestPesticideToDateKey"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPesticideReportBinding.inflate(inflater, container, false)
        val vw = binding?.root
        orchardViewModel.getFarmWithOrchardsMap().observe(viewLifecycleOwner) {
                farmWithOrchards ->
            farmOrchardsMap = farmWithOrchards
            val adapter = ArrayAdapter<String>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, farmWithOrchards.values.toList())
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.pesticideOrchardSpinner?.adapter = adapter
            binding?.pesticideOrchardSpinner?.onItemSelectedListener = this
        }

        datePicker()

        binding?.pesticideFromDate?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val date = binding?.pesticideFromDate?.text.toString()
                if(!Validator.validateDate(date)) {
                    Toast.makeText(requireContext(), "Invalid date format mm-dd-yyyy", Toast.LENGTH_LONG).show()
                } else {
                    isStartDateValid = true
                    if(isEndDateValid && orchardId > 0L) {
                        startDate = DateConverter().toOffsetDate(date)
                        getPesticideApplicationWithPesticides()
                    }
                }
            }
        })

        binding?.pesticideToDate?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val date = binding?.pesticideToDate?.text.toString()
                if(!Validator.validateDate(date)) {
                    Toast.makeText(requireContext(), "Invalid date format mm-dd-yyyy", Toast.LENGTH_LONG).show()
                } else {
                    isEndDateValid = true
                    if(isStartDateValid && orchardId > 0L) {
                        endDate = DateConverter().toOffsetDate(date)
                        getPesticideApplicationWithPesticides()
                    }
                }
            }
        })

        binding?.pesticideReportBtn?.setOnClickListener {
            createPDF()
        }

        return vw
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener(pesticideFromDateRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.pesticideFromDate?.setText(bundle.getString(pesticideDateKey))
            isStartDateValid = true
            if(isEndDateValid && orchardId > 0L) {
                startDate = DateConverter().toOffsetDate(bundle.getString(pesticideDateKey))
                getPesticideApplicationWithPesticides()
            }
        }
        childFragmentManager.setFragmentResultListener(pesticideToDateRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.pesticideToDate?.setText(bundle.getString(pesticideDateKey))
            isEndDateValid = true
            if(isStartDateValid && orchardId > 0L) {
                endDate = DateConverter().toOffsetDate(bundle.getString(pesticideDateKey))
                getPesticideApplicationWithPesticides()
            }
        }
    }

    private fun datePicker() {
        binding?.pesticideFromDateCal?.setOnClickListener {
            DatePickerFragment(pesticideFromDateRequestKey, pesticideDateKey)
                .show(childFragmentManager, getString(R.string.from_date))
        }

        binding?.pesticideToDateCal?.setOnClickListener {
            DatePickerFragment(pesticideToDateRequestKey, pesticideDateKey)
                .show(childFragmentManager, getString(R.string.to_date))
        }
    }

    private fun getPesticideApplicationWithPesticides() {
        pesticideViewModel.getPesticideApplicationWithPesticides(orchardId, startDate, endDate)
            .observe(viewLifecycleOwner) {
                    pesticideApplicationsWithPesticides ->
                val adapter = PesticideExpandableAdapter(pesticideApplicationsWithPesticides, requireContext())
                binding?.pesticideListView?.setAdapter(adapter)
            }
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val obj = parent?.adapter?.getItem(position)
        if(obj is String && !obj.isEmpty() && !farmOrchardsMap?.isEmpty()!!) {
            val key = this.farmOrchardsMap?.filter { it.value == obj }?.keys?.first()
            this.orchardId = key!!
            val ssDate = binding?.pesticideFromDate?.text.toString()
            val seDate = binding?.pesticideToDate?.text.toString()
            try {
                if(!ssDate.isEmpty() && !seDate.isEmpty()) {
                    startDate = DateConverter().toOffsetDate(ssDate)
                    endDate = DateConverter().toOffsetDate(seDate)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            getPesticideApplicationWithPesticides()
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
            Log.i("PesticideReportFragment", "the color is white")
            paint.setARGB(255,255,255,255)
        } else {
            //paint.color = Color.BLACK
            Log.i("PesticideReportFragment", "the color is white")
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
            val report = File(path, pesticideReport)
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