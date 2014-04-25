package com.wujy.dhuhelper.view;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wujy.dhuhelper.Globe;
import com.wujy.dhuhelper.R;
import com.wujy.dhuhelper.WindowActivity;
import com.wujy.dhuhelper.net.MyHttpRequest;
import com.wujy.dhuhelper.net.MyHttpResponse;
import com.wujy.dhuhelper.net.NetConstant;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class RunningQueryScreen extends WindowActivity {

	private ArrayList<String[]> hide_params = new ArrayList<String[]>();

	private TextView mInfoResult;
	private ImageView mImageView;
	private LinearLayout mLinearLayout;

	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {
		Document doc;
		if (myHttpResponse.getPipIndex() == NetConstant.RUNNING_LOGIN) {
			doc = myHttpResponse.getData();

			System.out.println("登陆体育查询页面成功");

            if (!doc.select("body:contains(欢迎使用学生查询系统)").isEmpty()) {
                MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET,
                        NetConstant.URL_RUNNING_QUERY, null, true);
                req.setPipIndex(NetConstant.RUNNING_QUERY);
                mNetClient.sendRequest(req);
            } else {
                mInfoResult.setText("获取失败");
            }

		} else if (myHttpResponse.getPipIndex() == NetConstant.RUNNING_QUERY) {
            doc = myHttpResponse.getData();

            System.out.println("获取体育查询页面成功");

            Elements etable = doc.getElementsByAttributeValue("bgcolor", "#6B7DBE").select("tbody:contains(体锻信息查询)");
            String table = etable.html();
            table = table.replace("<td colspan=\"2\" height=\"20\"><b>", "");
            table = table.replace("</b>", "");
            table = table.replace("\t", "");
            table = table.replace("\n", "");
            table = table.replace("&nbsp;&nbsp;&nbsp;&nbsp;", "\n  ");
            table = table.replace("&nbsp;", "");
            table = table.replace("<tr>", "");
            table = table.replace(" <td bgcolor=\"#FFFFFF\">", ": ");
            table = table.replace("</tr>", "\n");
            table = table.replace("</td> <td>", ": ");
            table = table.replace("<td height=\"30\" width=\"30%\" align=\"left\" bgcolor=\"#FFFFFF\">", "");
            table = table.replace("<td height=\"30\" align=\"left\" bgcolor=\"#FFFFFF\">", "");
            table = table.replace("<td>", "");
            table = table.replace("</td>", "");
            table = table.replace("</tr>", "");
            table = table.replace("早操", "\n   早操");

            mInfoResult.setText(table);
        }
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("体锻查询"); //$NON-NLS-1$

		setContentView(R.layout.running_query_layout);

		setSlidingMenu();

		mInfoResult = (TextView) findViewById(R.id.running_query_result);
		mInfoResult.setMovementMethod(ScrollingMovementMethod.getInstance());


        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("userName", Globe.sId_jw));
        params.add(new BasicNameValuePair("passwd", Globe.sId_jw));

        MyHttpRequest req = new MyHttpRequest(
                NetConstant.TYPE_POST, NetConstant.URL_RUNNING_LOGIN,
                params, true);
        req.setPipIndex(NetConstant.RUNNING_LOGIN);
        mNetClient.sendRequest(req);


		mImageView = (ImageView) findViewById(R.id.menu_icon_running_query);
		mImageView.setImageResource(R.drawable.ic_action_location_found);
		mLinearLayout = (LinearLayout) findViewById(R.id.menu_running_query);
		mLinearLayout.setBackgroundResource(R.drawable.menu2);
		mLinearLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.showContent();
			}
		});
	}
}
