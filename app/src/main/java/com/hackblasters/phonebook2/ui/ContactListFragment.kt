package com.hackblasters.phonebook2.ui

import com.hackblasters.phonebook2.R
//import com.hackblasters.phonebook2.Build
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hackblasters.phonebook2.BuildConfig
import com.hackblasters.phonebook2.data.Contact
import kotlinx.android.synthetic.main.fragment_contact_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*


const val READ_FILE_REQUEST = 1

class ContactListFragment : Fragment() {

    private lateinit var viewModel: ContactListViewModel

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        viewModel = ViewModelProviders.of(this)
            .get(ContactListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contact_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        with(contact_list){
            layoutManager = LinearLayoutManager(activity)
            adapter = ContactAdapter {
                val bund = Bundle()
                bund.putLong("id", it)
                findNavController().navigate(
                    R.id.action_contactListFragment_to_contactDetailFragment, bund)
//                    ContactListFragmentDirections.actionContactListFragmentToContactDetailFragment(
////                        it
//                    )
//                )
            }
            setHasFixedSize(true)

        }

        add_contact.setOnClickListener{

            findNavController().navigate(
                ContactListFragmentDirections.actionContactListFragmentToAddContactFragment(
//                    0
                )
            )
        }

        viewModel.contacts.observe(viewLifecycleOwner, Observer {
            (contact_list.adapter as ContactAdapter).submitList(it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_edit -> {
                importContacts()
                true
            }
            R.id.menu_delete -> {
                GlobalScope.launch {
                    exportContacts()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                READ_FILE_REQUEST -> {
                    GlobalScope.launch{
                        data?.data?.also { uri ->
                            readFromFile(uri)
                        }
                    }
                }
            }
        }
    }

    private fun importContacts(){
        Intent(Intent.ACTION_GET_CONTENT).also { readFileIntent ->
            readFileIntent.addCategory(Intent.CATEGORY_OPENABLE)
            readFileIntent.type = "text/*"
            readFileIntent.resolveActivity(requireActivity().packageManager)?.also {
                startActivityForResult(readFileIntent, READ_FILE_REQUEST)
            }
        }
    }

    private suspend fun readFromFile(uri: Uri){
        try {
            requireActivity().contentResolver.openFileDescriptor(uri, "r")?.use {
                withContext(Dispatchers.IO) {
                    FileInputStream(it.fileDescriptor).use {
                        parseCSVFile(it)
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private suspend fun parseCSVFile(stream: InputStream){
        val employees = mutableListOf<Contact>()

        BufferedReader(InputStreamReader(stream)).forEachLine {
            val tokens = it.split(",")
            employees.add(Contact(id = 0, name = tokens[0], number = tokens[1],
                address = tokens[2], photo = ""))
        }

        if(employees.isNotEmpty()){
            viewModel.insertContacts(employees)
        }
    }

    private suspend fun exportContacts(){
        var csvFile: File? = null
        withContext(Dispatchers.IO) {
            csvFile = try {
                createFile(requireActivity(),"Documents", "csv")
            } catch (ex: IOException) {
                Toast.makeText(requireActivity(), getString(R.string.file_create_error, ex.message),
                    Toast.LENGTH_SHORT). show()
                null
            }

            csvFile?.printWriter()?.use { out ->
                val employees = viewModel.getContactList()
                if(employees.isNotEmpty()){
                    employees.forEach{
                        out.println(it.name + "," + it.number)
                    }
                }
            }
        }
        withContext(Dispatchers.Main){
            csvFile?.let{
                val uri = FileProvider.getUriForFile(
                    requireActivity(), BuildConfig.APPLICATION_ID + ".fileprovider",
                    it)
                launchFile(uri, "csv")
            }
        }
    }

    private fun launchFile(uri: Uri, ext: String){
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(uri, mimeType)
        if(intent.resolveActivity(requireActivity().packageManager) != null){
            startActivity(intent)
        } else{
            Toast.makeText(requireActivity(), getString(R.string.no_app_csv), Toast.LENGTH_SHORT).show()
        }
    }
}
