package com.wujy.dhuhelper.view;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.wujy.dhuhelper.Globe;
import com.wujy.dhuhelper.R;
import com.wujy.dhuhelper.WindowActivity;
import com.wujy.dhuhelper.net.MyHttpRequest;
import com.wujy.dhuhelper.net.MyHttpResponse;
import com.wujy.dhuhelper.net.NetClient;
import com.wujy.dhuhelper.net.NetConstant;

public class ExamListScreen extends WindowActivity implements OnClickListener {
	private class ExamListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mExamListResults.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@SuppressLint("NewApi")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.exam_list_item_layout, null);
			}

			TextView col1 = (TextView) convertView.findViewById(R.id.col1);
			col1.setText(mExamListResults.get(position)[0]);
			TextView col2 = (TextView) convertView.findViewById(R.id.col2);
			col2.setText(mExamListResults.get(position)[1]);
			TextView col3 = (TextView) convertView.findViewById(R.id.col3);
			col3.setText(mExamListResults.get(position)[2]);
			TextView col4 = (TextView) convertView.findViewById(R.id.col4);
			col4.setText(mExamListResults.get(position)[3]);

			return convertView;
		}

	}

	private NetClient mNetClient = Globe.sNetClient;

	private ListView mResultListView;

	private ExamListAdapter mExamListAdapter;

	private ArrayList<String[]> mExamListResults = new ArrayList<String[]>();

	private Button mExamSearchBtn, mExamBkSearchBtn, mExamBkScoreBtn;

	private TextView col1, col2, col3, col4;

	private ImageView mImageView;
	private LinearLayout mLinearLayout;

	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {

		if (myHttpResponse.getPipIndex() == NetConstant.KCCX_BK_SCORE) {
			Document doc = myHttpResponse.getData();

			col1.setText("学期");
			col2.setText("课程编号");
			col3.setText("课程名称");
			col4.setText("成绩记录");

			mExamListResults.clear();

			Elements eles = doc.select("table:contains(序号)").select("tr");
			for (Element ele : eles) {
				Elements items = ele.getElementsByTag("td");
				String[] str = new String[items.size() - 1];
				if (items.get(0).text().contentEquals("序号"))
					continue;
				for (int i = 1; i < items.size(); i++) {
					int index = i;
					if (i == 3)
						index = 0;
					if (i == 4)
						index = 3;
					str[index] = items.get(i).text();
				}

				mExamListResults.add(str);
			}

			mExamListAdapter.notifyDataSetChanged();

			System.out.println("获取补考成绩记录页面信息成功");
		} else {
			Document doc = myHttpResponse.getData();

			col1.setText("考试日期");
			col2.setText("考试时间");
			col3.setText("课程名称");
			col4.setText("考试地点");

			mExamListResults.clear();
			// doc.getElementsByAttributeValue("border", "1");
			Elements eles = doc.getElementsByAttributeValue("border", "1")
					.select("tr");
			for (Element ele : eles) {
				Elements items = ele.getElementsByTag("td");
				String[] str = new String[items.size()];
				if (items.get(0).text().contentEquals("考试日期"))
					continue;
				for (int i = 0; i < items.size(); i++) {
					str[i] = items.get(i).text();
				}
				mExamListResults.add(str);
			}

			mExamListAdapter.notifyDataSetChanged();

			System.out.println("获取考场页面信息成功");
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		MyHttpRequest req;
		switch (v.getId()) {
		case R.id.exam_search_btn:
			// params.add(new BasicNameValuePair("yearTerm",
			// mTermSpinner.getSelectedItem().toString()));
			req = new MyHttpRequest(NetConstant.TYPE_POST,
					NetConstant.URL_KCCX, params, true);
			req.setPipIndex(NetConstant.KCCX_HOMEPAGE);
			mNetClient.sendRequest(req);
			break;
		case R.id.exam_bk_search_btn:
			// params.add(new BasicNameValuePair("yearTerm",
			// mTermSpinner.getSelectedItem().toString()));
			req = new MyHttpRequest(NetConstant.TYPE_GET,
					NetConstant.URL_KCCX_BK, params, true);
			req.setPipIndex(NetConstant.KCCX_BK_HOMEPAGE);
			mNetClient.sendRequest(req);
			break;
		case R.id.exam_bk_score_btn:
			// params.add(new BasicNameValuePair("yearTerm",
			// mTermSpinner.getSelectedItem().toString()));
			req = new MyHttpRequest(NetConstant.TYPE_GET,
					NetConstant.URL_CJCX_BK, params, true);
			req.setPipIndex(NetConstant.KCCX_BK_SCORE);
			mNetClient.sendRequest(req);
			break;
		default:
			break;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("考场(补考)查询");

		setContentView(R.layout.exam_search_layout);

		setSlidingMenu();

		System.out.println("cookie:" + Globe.sCookieString);
		System.out.println("进入考场查询页面");

		// mTermSpinner = (Spinner) findViewById(R.id.term_spinner);
		mExamSearchBtn = (Button) findViewById(R.id.exam_search_btn);
		mExamSearchBtn.setOnClickListener(this);
		mExamBkSearchBtn = (Button) findViewById(R.id.exam_bk_search_btn);
		mExamBkSearchBtn.setOnClickListener(this);
		mExamBkScoreBtn = (Button) findViewById(R.id.exam_bk_score_btn);
		mExamBkScoreBtn.setOnClickListener(this);
		mResultListView = (ListView) findViewById(R.id.exam_result_list);
		mExamListAdapter = new ExamListAdapter();
		mResultListView.setAdapter(mExamListAdapter);
		col1 = (TextView) findViewById(R.id.col1);
		col2 = (TextView) findViewById(R.id.col2);
		col3 = (TextView) findViewById(R.id.col3);
		col4 = (TextView) findViewById(R.id.col4);

		MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET,
				NetConstant.URL_KCCX, null, true);
		req.setPipIndex(NetConstant.KCCX_HOMEPAGE);
		mNetClient.sendRequest(req);

		mImageView = (ImageView) findViewById(R.id.menu_icon_exam);
		mImageView.setImageResource(R.drawable.ic_action_location_found);
		mLinearLayout = (LinearLayout) findViewById(R.id.menu_exam_search);
		mLinearLayout.setBackgroundResource(R.drawable.menu2);
		mLinearLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.showContent();
			}
		});
	}
}
