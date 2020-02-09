package at.shockbytes.dante.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.Languages

class ManualAddLanguageSpinnerAdapter(
    context: Context,
    private val languages: Array<Languages>
) : ArrayAdapter<Languages>(context, 0, languages) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val item = languages[position]
        var v = convertView
        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.item_language, parent, false)
        }
        v!!.findViewById<TextView>(R.id.item_language_text)?.text = item.name

        val imageView = v.findViewById<ImageView>(R.id.item_language_image)
        imageView.setImageResource(item.image)

        return v
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }
}