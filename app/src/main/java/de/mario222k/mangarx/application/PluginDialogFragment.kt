package de.mario222k.mangarx.application

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import de.mario222k.mangarx.R
import de.mario222k.mangarx.plugin.PluginDetail
import de.mario222k.mangarx.plugin.PluginProvider
import kotlinx.android.synthetic.main.fragment_plugins.view.*
import javax.inject.Inject

open class PluginDialogFragment : DialogFragment() {

    @Inject
    lateinit var pluginProvider: PluginProvider

    private var layout: View? = null
    private var selectListener: PluginSelectListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity!!

        (activity.application as? MyApp)?.pluginComponent?.inject(this)

        layout = activity.layoutInflater?.inflate(R.layout.fragment_plugins, null)
        layout?.list_view?.layoutManager = LinearLayoutManager(activity)
        onListenerChanged()

        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(true)
        builder.setView(layout)
        return builder.create()
    }

    fun setPluginSelectListener(listener: PluginSelectListener?) {
        selectListener = listener
        onListenerChanged()
    }

    private fun onListenerChanged() {
        layout?.list_view?.adapter = PluginAdapter(pluginProvider, selectListener)
        onAdapterChanged()
    }

    private fun onAdapterChanged() {
        layout?.list_view?.let {
            if (it.adapter?.itemCount ?: 0 == 0) {
                it.visibility = View.GONE
                layout?.empty_text?.visibility = View.VISIBLE
            } else {
                it.visibility = View.VISIBLE
                layout?.empty_text?.visibility = View.GONE
            }
        }
    }
}

private class PluginAdapter(provider: PluginProvider?, listener: PluginSelectListener? = null) : RecyclerView.Adapter<PluginViewHolder>() {
    internal var selectListener = listener
    private val pluginProvider = provider

    override fun onBindViewHolder(viewHolder: PluginViewHolder, index: Int) {
        val plugin = pluginProvider?.plugins?.get(index)
        val context = viewHolder.context

        viewHolder.icon.setImageDrawable(plugin?.getIcon(context))
        viewHolder.name.text = plugin?.getName(context)
        viewHolder.version.text = plugin?.getVersion(context)
        viewHolder.itemView?.setOnClickListener({ v ->
            pluginProvider?.activePlugin = plugin
            selectListener?.onPluginSelect(plugin)
        })
    }

    override fun onCreateViewHolder(container: ViewGroup, viewType: Int): PluginViewHolder {
        val inflater = LayoutInflater.from(container.context)
        return PluginViewHolder(inflater.inflate(R.layout.layout_plugin_item, null))
    }

    override fun getItemCount() = pluginProvider?.plugins?.size ?: 0
}

interface PluginSelectListener {
    fun onPluginSelect(plugin: PluginDetail?)
}

private class PluginViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val context = view.context
    val icon = view.findViewById(R.id.plugin_icon) as ImageView
    val name = view.findViewById(R.id.plugin_name) as TextView
    val version = view.findViewById(R.id.plugin_version) as TextView
}
