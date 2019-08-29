package com.example.secretmessagingapp.views

import com.example.secretmessagingapp.R
import com.example.secretmessagingapp.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class UserItem(val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        // bind each list object inside the recycler view
        viewHolder.itemView.username_textview_newmessage.text = user.username
        // Load images using image caching library
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.image_imageview_newmessage)
    }

    override fun getLayout() : Int{
        return R.layout.user_row_new_message
    }
}