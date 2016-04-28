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
import javax.inject.Inject

open class PluginDialogFragment() : DialogFragment() {

    @Inject
    lateinit var pluginProvider: PluginProvider

    private var layout: View? = null
    private var recyclerView: RecyclerView? = null
    private var emptyText: View? = null
    private var selectListener: PluginSelectListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        (activity.application as MyApp).pluginComponent.inject(this)

        initLayout()

        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(true)
        builder.setView(layout)
        return builder.create()
    }

    fun setPluginSelectListener(listener: PluginSelectListener?) {
        selectListener = listener
        onListenerChanged()
    }

    private fun initLayout() {
        layout = activity.layoutInflater.inflate(R.layout.fragment_plugins, null) as ViewGroup
        recyclerView = layout?.findViewById(R.id.list_view) as RecyclerView
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        emptyText = layout?.findViewById(R.id.empty_text)

        onListenerChanged()
    }

    private fun onListenerChanged() {
        recyclerView?.adapter = PluginAdapter(pluginProvider, selectListener)
        onAdapterChanged()
    }

    private fun onAdapterChanged() {
        if (recyclerView?.adapter?.itemCount ?: 0 == 0) {
            recyclerView?.visibility = View.GONE
            emptyText?.visibility = View.VISIBLE
        } else {
            recyclerView?.visibility = View.VISIBLE
            emptyText?.visibility = View.GONE
        }
    }
}

private class PluginAdapter(provider: PluginProvider?, listener: PluginSelectListener? = null) : RecyclerView.Adapter<PluginViewHolder>() {
    internal var selectListener = listener
    private val pluginProvider = provider

    override fun onBindViewHolder(viewHolder: PluginViewHolder?, index: Int) {
        if(viewHolder == null) {
            return
        }

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

    override fun onCreateViewHolder(container: ViewGroup?, viewType: Int): PluginViewHolder? {
        val inflater = LayoutInflater.from(container?.context)
        val viewHolder = PluginViewHolder(inflater.inflate(R.layout.layout_plugin_item, null))
        return viewHolder
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
