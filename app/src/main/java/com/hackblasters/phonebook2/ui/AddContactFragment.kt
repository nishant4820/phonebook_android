package com.hackblasters.phonebook2.ui


import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.hackblasters.phonebook2.BuildConfig
import com.hackblasters.phonebook2.R
import com.hackblasters.phonebook2.data.Contact
import kotlinx.android.synthetic.main.fragment_add_contact.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


const val PERMISSION_REQUEST_CAMERA = 0
const val CAMERA_PHOTO_REQUEST = 1
const val GALLERY_PHOTO_REQUEST = 2

class AddContactFragment : Fragment() {

    private lateinit var viewModel: ContactDetailViewModel

    //For simplicity this variable is here but should be moved to ViewModel
    private var selectedPhotoPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this)
            .get(ContactDetailViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_contact, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


//        val id = ContactDetailFragmentArgs.fromBundle(requireArguments()).id
        val id = arguments?.getLong("id")
        Log.d("MYTAG", "$id")
        id?.let { viewModel.setContactId(it) }

        viewModel.contact.observe(viewLifecycleOwner, Observer {
            it?.let { setData(it) }
        })
//        contact_photo_add.setImageResource(R.drawable.blank_photo)

        save_contact.setOnClickListener {
            saveContact()
        }

        contact_photo_add.setOnClickListener {
            contact_photo_add.setImageResource(R.drawable.blank_photo)
            contact_photo_add.tag = ""
        }

        photo_from_camera.setOnClickListener {
            clickPhotoAfterPermission(it)
        }

        photo_from_gallery.setOnClickListener {
            pickPhoto()
        }
    }

    private fun setData(contact: Contact) {
        with(contact.photo) {
            if (isNotEmpty()) {
                contact_photo_add.setImageURI(Uri.parse(this))
                contact_photo_add.tag = this

            } else {
                contact_photo_add.setImageResource(R.drawable.blank_photo)
                contact_photo_add.tag = ""
            }
        }

        contact_name_add.setText(contact.name)
        contact_number_add.setText(contact.number)
        contact_address_add.setText(contact.address)
    }

    private fun saveContact() {
        val name = contact_name_add.text.toString()
        val number = contact_number_add.text.toString()
        val address = contact_address_add.text.toString()
        val photo = contact_photo_add.tag as String

        val contact = Contact(viewModel.contactId.value!!, name, number, address, photo)
        viewModel.saveContact(contact)

        requireActivity().onBackPressed()
    }

    private fun deleteContact() {
        viewModel.deleteContact()

        requireActivity().onBackPressed()
    }

    private fun clickPhotoAfterPermission(view: View) {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            clickPhoto()
        } else {
            requestCameraPermission(view)
        }
    }

    private fun requestCameraPermission(view: View) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.CAMERA
            )
        ) {
            val snack = Snackbar.make(view, R.string.camera_permission, Snackbar.LENGTH_INDEFINITE)
            snack.setAction("OK", View.OnClickListener {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSION_REQUEST_CAMERA
                )
            })
            snack.show()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CAMERA
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                clickPhoto()
            } else {
                Toast.makeText(
                    requireActivity(), "Permission denied to use camera",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun clickPhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                val photoFile: File? = try {
                    createFile(requireActivity(), Environment.DIRECTORY_PICTURES, "jpg").apply {
                        selectedPhotoPath = absolutePath
                    }
                } catch (ex: IOException) {
                    Toast.makeText(
                        requireActivity(), getString(R.string.file_create_error, ex.message),
                        Toast.LENGTH_SHORT
                    ).show()
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireActivity(),
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA_PHOTO_REQUEST)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_PHOTO_REQUEST -> {
                    val file = File(selectedPhotoPath)
                    val uri = Uri.fromFile(file)
                    contact_photo_add.setImageURI(uri)
                    contact_photo_add.tag = uri.toString()
                }
                GALLERY_PHOTO_REQUEST -> {
                    data?.data?.also { uri ->
                        val photoFile: File? = try {
                            createFile(
                                requireActivity(),
                                Environment.DIRECTORY_PICTURES,
                                "jpg"
                            ).apply {
                                selectedPhotoPath = absolutePath
                            }
                        } catch (ex: IOException) {
                            Toast.makeText(
                                requireActivity(),
                                getString(R.string.file_create_error, ex.message),
                                Toast.LENGTH_SHORT
                            ).show()
                            null
                        }
                        photoFile?.also {
                            try {
                                val resolver = requireActivity().contentResolver
                                resolver.openInputStream(data!!.data!!).use { stream ->
                                    val output = FileOutputStream(photoFile)
                                    stream!!.copyTo(output)
                                }
                                val uri = Uri.fromFile(photoFile)
                                contact_photo_add.setImageURI(uri)
                                contact_photo_add.tag = uri.toString()
                            } catch (e: FileNotFoundException) {
                                e.printStackTrace()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }


    private fun pickPhoto() {
        val pickPhotoIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickPhotoIntent.resolveActivity(requireActivity().packageManager)?.also {
            startActivityForResult(pickPhotoIntent, GALLERY_PHOTO_REQUEST)
        }

    }
}
