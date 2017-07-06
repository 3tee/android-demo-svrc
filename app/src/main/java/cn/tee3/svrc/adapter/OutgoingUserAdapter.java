package cn.tee3.svrc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.tee3.avd.User;
import cn.tee3.svrc.Constants;
import cn.tee3.svrc.R;

/**
 * 外呼设备用户列表返回Adapter
 * Created by shengf on 2017/6/22.
 */

public class OutgoingUserAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<User> outgoingUsers;//外呼设备用户列表

    public OutgoingUserAdapter(Context context, List<User> outgoingUsers) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.outgoingUsers = outgoingUsers;
    }

    @Override
    public int getCount() {
        return outgoingUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return outgoingUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View curView = convertView;
        if (curView == null) {
            curView = inflater.inflate(R.layout.user_item, parent, false);
        }
        ViewHolder tHolder = (ViewHolder) curView.getTag();
        if (tHolder == null) {
            tHolder = new ViewHolder();
        }
        User user = outgoingUsers.get(position);
        tHolder.tvUserName = (TextView) curView.findViewById(R.id.tv_user_name);
        tHolder.tvUserName.setText(user.getUserName());
        if (user.getUserId().equals(Constants.SELECT_OUTGOING_USER_ID)) {
            curView.setBackgroundResource(R.color.itemSelectBg);
        } else {
            curView.setBackgroundResource(R.color.itemBg);
        }
        curView.setTag(tHolder);
        return curView;
    }

    public final class ViewHolder {
        private TextView tvUserName;
    }
}