package com.orchardmanager.treedata.ui.irrigation

import android.content.Context
import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
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
import androidx.lifecycle.Observer
import com.orchardmanager.treedata.R
import com.orchardmanager.treedata.data.DateConverter
import com.orchardmanager.treedata.data.Validator
import com.orchardmanager.treedata.databinding.FragmentIrrigationReportBinding
import com.orchardmanager.treedata.entities.FlowRateUnit
import com.orchardmanager.treedata.ui.orchard.OrchardViewModel
import com.orchardmanager.treedata.utils.DatePickerFragment
import com.orchardmanager.treedata.utils.TimePickerFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.Calendar


@AndroidEntryPoint
class IrrigationReportFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val irrigationViewModel: IrrigationViewModel by viewModels()
    private val orchardViewModel: OrchardViewModel by viewModels()
    private val irrigationReport = "IrrigationReport.pdf"
    private var _binding: FragmentIrrigationReportBinding? = null
    private val binding get() = _binding
    private var orchardId: Long = 0L
    private var farmOrchardsMap: kotlin.collections.Map<Long, String>? = null
    private val calStart = Calendar.getInstance()
    private val yearStart = calStart.get(Calendar.YEAR)
    private var startDate = LocalDate.of(yearStart, 1, 1)
    private var endDate = LocalDate.of(yearStart, 12, 31)
    private var totalHours: Long = 0L
    private val irrigationDateKey = "irrigationDate"
    private val irrigationFromDateRequestKey = "requestIrrigationFromDateeKey"
    private val irrigationToDateRequestKey = "requestIrrigationToDateKey"
    private var isStartDateValid = false
    private var isEndDateValid = false
    //private var window: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIrrigationReportBinding.inflate(inflater, container, false)
        val vw = binding?.root

        orchardViewModel.getFarmWithOrchardsMap().observe(viewLifecycleOwner, Observer {
                farmWithOrchards ->
            farmOrchardsMap = farmWithOrchards
            val adapter = ArrayAdapter<String>(requireContext(), R.layout.farm_spinner_layout,
                R.id.textViewFarmSpinner, farmWithOrchards.values.toList())
            adapter.setDropDownViewResource(R.layout.farm_spinner_layout)
            binding?.irrigationOrchardSpinner?.adapter = adapter
            binding?.irrigationOrchardSpinner?.onItemSelectedListener = this
        })

        binding?.irrigationFromDate?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val date = binding?.irrigationFromDate?.text.toString()
                if(!Validator.validateDate(date)) {
                    Toast.makeText(requireContext(), "Invalid date format mm-dd-yyyy", Toast.LENGTH_LONG).show()
                } else {
                    isStartDateValid = true
                    if(isEndDateValid && orchardId > 0L) {
                        startDate = DateConverter().toOffsetDate(date)
                        getIrrigationTotalHours()
                        getIrrigationCumulativeFlorRate()
                    }
                }
            }
        })

        binding?.irrigationToDate?.setOnFocusChangeListener(object: View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                val date = binding?.irrigationToDate?.text.toString()
                if(!Validator.validateDate(date)) {
                    Toast.makeText(requireContext(), "Invalid date format mm-dd-yyyy", Toast.LENGTH_LONG).show()
                } else {
                    isEndDateValid = true
                    if(isStartDateValid && orchardId > 0L) {
                        endDate = DateConverter().toOffsetDate(date)
                        getIrrigationTotalHours()
                        getIrrigationCumulativeFlorRate()
                    }
                }
            }
        })

        datePicker()

        binding?.irrigationReportBtn?.setOnClickListener(View.OnClickListener {
            createPDF()
        })
        return vw
    }

    private fun getIrrigationTotalHours() {
        if(orchardId != null && startDate != null && endDate != null) {
            irrigationViewModel.getIrrigationsTotalHours(orchardId, startDate, endDate).observe(viewLifecycleOwner, Observer{
                    totalHours ->
                this.totalHours = totalHours
                binding?.totalIrrigationHours?.setText(totalHours.toString())
            })
        }
    }

    private fun getIrrigationCumulativeFlorRate() {
        irrigationViewModel.getPumpWithIrrigationSystem(orchardId).observe(viewLifecycleOwner, Observer {
                pumpWithIrrigationSystem ->
            val unit = ""
            val flowRate = pumpWithIrrigationSystem.pump.flowRate
            val flowUnit = pumpWithIrrigationSystem.pump.flowRateUnit
            if(flowUnit.equals(FlowRateUnit.GALLONSPERMINUTE)) {
                val gallonsPumped = flowRate*60*totalHours
                binding?.totalIrrigationGallons?.setText(gallonsPumped.toString())
            } else {
                val gallonsPumped = flowRate*totalHours
                binding?.totalIrrigationGallons?.setText(gallonsPumped.toString())
            }
        })
    }

    private fun datePicker() {
        binding?.irrigationFromDateCal?.setOnClickListener(View.OnClickListener {
            DatePickerFragment(irrigationFromDateRequestKey, irrigationDateKey)
                .show(childFragmentManager, getString(R.string.from_date))
        })

        binding?.irrigationToDateCal?.setOnClickListener(View.OnClickListener {
            DatePickerFragment(irrigationToDateRequestKey, irrigationDateKey)
                .show(childFragmentManager, getString(R.string.to_date))
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener(irrigationFromDateRequestKey, requireActivity()) {
                dateKey, bundle -> binding?.irrigationFromDate?.setText(bundle.getString(irrigationDateKey))
            isStartDateValid = true
            if(isEndDateValid && orchardId > 0L) {
                startDate = DateConverter().toOffsetDate(bundle.getString(irrigationDateKey))
                getIrrigationTotalHours()
                getIrrigationCumulativeFlorRate()
            }
        }
        childFragmentManager.setFragmentResultListener(irrigationToDateRequestKey, requireActivity()) {
            dateKey, bundle -> binding?.irrigationToDate?.setText(bundle.getString(irrigationDateKey))
            isEndDateValid = true
            if(isStartDateValid && orchardId > 0L) {
                endDate = DateConverter().toOffsetDate(bundle.getString(irrigationDateKey))
                getIrrigationTotalHours()
                getIrrigationCumulativeFlorRate()
            }
        }
    }

    fun Context.isDarkThemeOn(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private fun createPDF() {
        val document = PdfDocument()

        // create a page description
        val pageInfo = PageInfo.Builder(576, 792, 1).create()

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
            Log.i("IrrigationReportFragment", "the color is white")
            paint.setARGB(255,255,255,255)
        } else {
            //paint.color = Color.BLACK
            Log.i("IrrigationReportFragment", "the color is white")
            paint.setARGB(0,0,0,0)
        }
        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.DEFAULT
        canvas.drawPaint(paint)
        paint.textSize = 14f
        Log.i("IrrigationReportFragment", "the paint color is " + paint.color.toString())

        // draw something on the page
        view?.draw(canvas)

        // finish the page
        document.finishPage(page);
        // add more pages
        var outputStream: FileOutputStream? = null
        try {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val report = File(path, irrigationReport)
            outputStream = FileOutputStream(report.path)
            // write the document content
            document.writeTo(outputStream);
        } catch (e: Exception) {

        } finally {
            outputStream!!.flush()
            outputStream!!.close()
            // close the document
            document.close();
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val obj = parent?.adapter?.getItem(position)
        if(obj is String && !obj.isEmpty() && !farmOrchardsMap?.isEmpty()!!) {
            val key = this.farmOrchardsMap?.filter { it.value == obj }?.keys?.first()
            this.orchardId = key!!
            val ssDate = binding?.irrigationFromDate?.text.toString()
            val seDate = binding?.irrigationToDate?.text.toString()
            try {
                if(!ssDate.isEmpty() && !seDate.isEmpty()) {
                    startDate = DateConverter().toOffsetDate(ssDate)
                    endDate = DateConverter().toOffsetDate(seDate)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            getIrrigationTotalHours()

            getIrrigationCumulativeFlorRate()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }


}