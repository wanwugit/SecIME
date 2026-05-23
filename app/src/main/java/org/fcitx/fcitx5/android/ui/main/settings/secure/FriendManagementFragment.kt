/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.ui.main.settings.secure

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
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
import org.fcitx.fcitx5.android.data.secure.db.Friend

class FriendManagementFragment : Fragment() {

    private val friends = mutableListOf<Friend>()
    private lateinit var adapter: FriendAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        adapter = FriendAdapter(friends) { position -> showEditDialog(friends[position], position) }
        val recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = this@FriendManagementFragment.adapter
        }
        val fab = FloatingActionButton(requireContext()).apply {
            setImageResource(android.R.drawable.ic_input_add)
            setOnClickListener { showEditDialog(null, -1) }
        }
        return android.widget.FrameLayout(requireContext()).apply {
            addView(recyclerView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            addView(fab, android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.END or android.view.Gravity.BOTTOM
            ).apply { marginEnd = 32; bottomMargin = 32 })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFriends()
    }

    private fun loadFriends() {
        lifecycleScope.launch {
            val list = withContext(Dispatchers.IO) { SecureDataManager.getFriendDao().getAll() }
            friends.clear()
            friends.addAll(list)
            adapter.notifyDataSetChanged()
        }
    }

    private fun showEditDialog(existing: Friend?, position: Int) {
        val ctx = requireContext()
        val userIdEdit = EditText(ctx).apply { hint = getString(R.string.friend_user_id); inputType = InputType.TYPE_CLASS_TEXT }
        val remarkEdit = EditText(ctx).apply { hint = getString(R.string.friend_remark); inputType = InputType.TYPE_CLASS_TEXT }
        val phoneEdit = EditText(ctx).apply { hint = getString(R.string.friend_phone); inputType = InputType.TYPE_CLASS_PHONE }
        if (existing != null) {
            userIdEdit.setText(existing.userId)
            remarkEdit.setText(existing.remark ?: "")
            phoneEdit.setText(existing.phone ?: "")
        }
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (8 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
            addView(userIdEdit)
            addView(remarkEdit)
            addView(phoneEdit)
        }
        val title = if (existing != null) "编辑好友" else "添加好友"
        AlertDialog.Builder(ctx)
            .setTitle(title)
            .setView(layout)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val friend = if (existing != null) {
                    Friend(id = existing.id, userId = userIdEdit.text.toString(), remark = remarkEdit.text.toString().ifBlank { userIdEdit.text.toString() }, phone = phoneEdit.text.toString())
                } else {
                    Friend(userId = userIdEdit.text.toString(), remark = remarkEdit.text.toString().ifBlank { userIdEdit.text.toString() }, phone = phoneEdit.text.toString())
                }
                if (existing != null) {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) { SecureDataManager.getFriendDao().update(friend) }
                        loadFriends()
                    }
                } else {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) { SecureDataManager.getFriendDao().insert(friend) }
                        loadFriends()
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    class FriendAdapter(
        private val items: List<Friend>,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.Adapter<FriendAdapter.VH>() {

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
            val f = items[position]
            holder.title.text = f.remark ?: f.userId
            holder.subtitle.text = f.userId
            holder.itemView.setOnClickListener { onClick(position) }
        }

        override fun getItemCount() = items.size
    }
}