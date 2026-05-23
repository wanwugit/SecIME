/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.ui.main.settings.secure

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.secure.SecureDataManager
import org.fcitx.fcitx5.android.data.secure.db.DisguiseTemplate

class TemplateManagementFragment : Fragment() {

    private val templates = mutableListOf<DisguiseTemplate>()
    private lateinit var adapter: TemplateAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        adapter = TemplateAdapter(templates) { position -> showEditDialog(templates[position], position) }
        val recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = this@TemplateManagementFragment.adapter
        }
        val fab = FloatingActionButton(requireContext()).apply {
            setImageResource(android.R.drawable.ic_input_add)
            setOnClickListener { showEditDialog(null, -1) }
        }
        return android.widget.FrameLayout(requireContext()).apply {
            addView(recyclerView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            addView(fab, android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.END or Gravity.BOTTOM
            ).apply { marginEnd = 32; bottomMargin = 32 })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadTemplates()
    }

    private fun loadTemplates() {
        lifecycleScope.launch {
            val list = withContext(Dispatchers.IO) { SecureDataManager.getDisguiseTemplateDao().getAll() }
            templates.clear()
            templates.addAll(list)
            adapter.notifyDataSetChanged()
        }
    }

    private fun showEditDialog(existing: DisguiseTemplate?, position: Int) {
        val ctx = requireContext()
        val nameEdit = EditText(ctx).apply { hint = getString(R.string.template_name) }
        val prefixEdit = EditText(ctx).apply { hint = getString(R.string.template_prefix); inputType = InputType.TYPE_CLASS_TEXT }
        val suffixEdit = EditText(ctx).apply { hint = getString(R.string.template_suffix); inputType = InputType.TYPE_CLASS_TEXT }
        if (existing != null) {
            nameEdit.setText(existing.name)
            prefixEdit.setText(existing.prefix)
            suffixEdit.setText(existing.suffix)
        }
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (8 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
            addView(nameEdit)
            addView(prefixEdit)
            addView(suffixEdit)
        }
        val title = if (existing != null) "编辑模板" else "添加模板"
        AlertDialog.Builder(ctx)
            .setTitle(title)
            .setView(layout)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val template = DisguiseTemplate(name = nameEdit.text.toString(), prefix = prefixEdit.text.toString(), suffix = suffixEdit.text.toString(), isBuiltin = existing?.isBuiltin ?: false, createdAt = existing?.createdAt ?: System.currentTimeMillis())
                template.id = existing?.id ?: 0
                if (existing != null) {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) { SecureDataManager.getDisguiseTemplateDao().update(template) }
                        loadTemplates()
                    }
                } else {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) { SecureDataManager.getDisguiseTemplateDao().insert(template) }
                        loadTemplates()
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    class TemplateAdapter(
        private val items: List<DisguiseTemplate>,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.Adapter<TemplateAdapter.VH>() {

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(android.R.id.title)
            val subtitle: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                val pad = (16 * resources.displayMetrics.density).toInt()
                setPadding(pad, pad, pad, pad)
                addView(TextView(context).apply { id = android.R.id.title; textSize = 16f })
                addView(TextView(context).apply { id = android.R.id.text1; textSize = 14f; setTextColor(0xFF888888.toInt()) })
            }
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val t = items[position]
            holder.title.text = t.name
            holder.subtitle.text = "${t.prefix}%s${t.suffix}"
            holder.itemView.setOnClickListener { onClick(position) }
        }

        override fun getItemCount() = items.size
    }
}