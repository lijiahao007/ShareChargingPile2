package com.lijiahao.sharechargingpile2.ui.publishStationModule

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.lijiahao.sharechargingpile2.databinding.FragmentPileQRCodeGenerateBinding
import com.lijiahao.sharechargingpile2.di.GlideApp
import com.lijiahao.sharechargingpile2.utils.GlideUtils
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.R)
class PileQRCodeGenerateFragment : Fragment() {

    private val binding: FragmentPileQRCodeGenerateBinding by lazy {
        FragmentPileQRCodeGenerateBinding.inflate(layoutInflater)
    }

    private val args: PileQRCodeGenerateFragmentArgs by navArgs()

    private val qrcodeUrl: String by lazy {
        args.qrcodeUrl
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initUI()
        return binding.root
    }

    private fun initUI() {
        context?.let {
            val path = GlideUtils.getPileQRCodeRemotePath(qrcodeUrl)
            GlideApp.with(it).load(path).into(binding.ivQrcode)
        }

        //保存图片
        binding.btnSave.setOnClickListener {
            val bitmap = binding.ivQrcode.drawToBitmap()
            saveImageToGallery2(requireContext(), bitmap)
        }
    }


    public fun saveImageToGallery2(context: Context, image: Bitmap) {
        val mImageTime = System.currentTimeMillis();
        val imageDate = SimpleDateFormat("yyyyMMdd-HHmmss").format(Date(mImageTime));
        val SCREENSHOT_FILE_NAME_TEMPLATE = "winetalk_%s.png";//图片名称，以"winetalk"+时间戳命名
        val mImageFileName = String.format(SCREENSHOT_FILE_NAME_TEMPLATE, imageDate);

        val values = ContentValues();
        values.put(
            MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES
                    + File.separator + "winetalk"
        ); //Environment.DIRECTORY_SCREENSHOTS:截图,图库中显示的文件夹名。"dh"
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, mImageFileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
        values.put(MediaStore.MediaColumns.DATE_ADDED, mImageTime / 1000);
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, mImageTime / 1000);
        values.put(
            MediaStore.MediaColumns.DATE_EXPIRES,
            (mImageTime + DateUtils.DAY_IN_MILLIS) / 1000
        );
        values.put(MediaStore.MediaColumns.IS_PENDING, 1);

        val resolver = context.contentResolver;
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try {
            // First, write the actual data for our screenshot
            uri?.let {
                val out = resolver.openOutputStream(uri)
                if (!image.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    throw IOException("Failed to compress");
                }
                // Everything went well above, publish it!
                values.clear();
                values.put(MediaStore.MediaColumns.IS_PENDING, 0);
                values.putNull(MediaStore.MediaColumns.DATE_EXPIRES);
                resolver.update(uri, values, null, null);
                Toast.makeText(
                    context, "保存成功",
                    Toast.LENGTH_SHORT
                ).show();
            }

        } catch (e: Exception) {
            if (uri != null) {
                resolver.delete(uri, null)
            };
            Toast.makeText(
                context,
                "保存失败",
                Toast.LENGTH_SHORT
            ).show();

        }
    }


}