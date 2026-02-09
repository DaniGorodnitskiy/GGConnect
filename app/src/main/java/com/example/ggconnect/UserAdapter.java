package com.example.ggconnect;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_card, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvUserName.setText(user.getName() != null ? user.getName() : context.getString(R.string.no_name));
        holder.gamesContainer.removeAllViews();

        if (user.getGames() != null && !user.getGames().isEmpty()) {
            for (String gameName : user.getGames()) {
                addGameIcon(holder.gamesContainer, gameName);
            }
        }

        // Handle online/offline status text
        if (user.isOnline()) {
            holder.tvStatus.setText(context.getString(R.string.online_now));
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvStatus.setText(context.getString(R.string.offline));
            holder.tvStatus.setTextColor(Color.GRAY);
        }

        // --- Handle profile image (Base64) ---
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            try {
                byte[] bytes = android.util.Base64.decode(user.getProfileImageUrl(), android.util.Base64.DEFAULT);
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.ivProfileImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                holder.ivProfileImage.setImageResource(android.R.drawable.sym_def_app_icon);
            }
        } else {
            holder.ivProfileImage.setImageResource(android.R.drawable.sym_def_app_icon);
        }

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, UserProfileActivity.class);
            intent.putExtra("userId", user.getUid());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private void addGameIcon(LinearLayout container, String gameName) {
        ImageView imageView = new ImageView(context);
        int sizeInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 40, context.getResources().getDisplayMetrics());
        int marginInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
        params.setMargins(marginInPx, 0, marginInPx, 0);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        String fileName = gameName.toLowerCase().trim()
                .replace(" ", "_")
                .replace("-", "_")
                .replace(":", "")
                .replace("'", "");

        if (fileName.contains("stardew")) fileName = "stardew_valley";
        else if (fileName.contains("maple")) fileName = "maplestory";
        else if (fileName.contains("counter") || fileName.contains("csgo")) fileName = "csgo";
        else if (fileName.contains("league") || fileName.contains("lol")) fileName = "league_of_legends";

        int resId = context.getResources().getIdentifier(fileName, "drawable", context.getPackageName());
        if (resId != 0) {
            imageView.setImageResource(resId);
            container.addView(imageView);
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_help);
            container.addView(imageView);
        }
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvStatus;
        LinearLayout gamesContainer;
        ImageView ivProfileImage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            gamesContainer = itemView.findViewById(R.id.gamesContainer);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
        }
    }

    public void setFilteredList(List<User> filteredList) {
        this.userList = filteredList;
        notifyDataSetChanged();
    }
}