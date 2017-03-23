/*
 * MIT License
 *
 * Copyright (c) [2017] [velli20]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.velli.commander.collections;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.velli.commander.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;



public class AdapterConsoleCommands extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_RECEIVED = 0;
    private static final int VIEW_TYPE_SENT = 1;

    private final LayoutInflater mInflater;
    private final DateFormat formOut = new SimpleDateFormat("dd.MM.yyyy ' 'HH:mm:ss", Locale.getDefault());
    private ArrayList<Command> mCommands;

    @Override
    public int getItemViewType(int position) {
        return mCommands.get(position).isIncomingCommand() ? VIEW_TYPE_RECEIVED : VIEW_TYPE_SENT;
    }

    public AdapterConsoleCommands(Context c) {
        mInflater = LayoutInflater.from(c);

        /* Change tint of the arrow drawables */
        Drawable arrowLeft = DrawableCompat.wrap(c.getResources().getDrawable(R.mipmap.ic_arrow_left_bold_grey));
        Drawable arrowRight = DrawableCompat.wrap(c.getResources().getDrawable(R.mipmap.ic_arrow_right_bold_grey));

        DrawableCompat.setTint(arrowLeft, c.getResources().getColor(R.color.colorPrimary));
        DrawableCompat.setTint(arrowRight, c.getResources().getColor(R.color.colorPrimary));
    }

    public void addCommandsList(ArrayList<Command> list) {
        mCommands = list;
        notifyDataSetChanged();
    }

    public void addCommand(Command command) {
        if(mCommands == null) {
            mCommands = new ArrayList<>();
        }
        mCommands.add(command);
        notifyItemInserted(mCommands.size()-1);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolderCommand viewHolderCommand = new ViewHolderCommand(mInflater.inflate(viewType == VIEW_TYPE_RECEIVED ?
                R.layout.list_item_command_received : R.layout.list_item_command_sent, parent, false));
        return viewHolderCommand;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Command command = mCommands.get(position);

        ((ViewHolderCommand)holder).mMessage.setText(command.getCommand());
        ((ViewHolderCommand)holder).mDate.setText(formOut.format(new Date(command.getDate())));
    }

    @Override
    public int getItemCount() {
        if (mCommands == null) {
            return 0;
        }
        return mCommands.size();
    }
}
