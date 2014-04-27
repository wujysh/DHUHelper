package org.sjutas.dhu.view;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.sjutas.dhu.Globe;
import org.sjutas.dhu.R;
import org.sjutas.dhu.WindowActivity;
import org.sjutas.dhu.net.MyHttpRequest;
import org.sjutas.dhu.net.MyHttpResponse;
import org.sjutas.dhu.net.NetClient;
import org.sjutas.dhu.net.NetConstant;

public class PointSearchScreen extends WindowActivity implements
		OnClickListener {
	private class PointListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mPointListResults.size();
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

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.point_list_item_layout, null);
			}
			TextView point_term = (TextView) convertView
					.findViewById(R.id.point_term);
			point_term.setText(mPointListResults.get(position)[0]);
			TextView point_point = (TextView) convertView
					.findViewById(R.id.point_point);
			point_point.setText(mPointListResults.get(position)[1]);

			return convertView;
		}

	}

	private int mCurSelectTermIndex;

	private NetClient mNetClient = Globe.sNetClient;

	private ListView mResultListView;

	private PointListAdapter mPointListAdapter;

	private ArrayList<String[]> mPointListResults = new ArrayList<String[]>();

	private Button mPointSearchBtn;
	private TextView mJdTextView;

	private ArrayAdapter<String> mTermSpinnerAdapter;

	private String[] mTermStrs;

	private Spinner mTermStartSpinner, mTermEndSpinner;

	private ImageView mImageView;
	private LinearLayout mLinearLayout;

	@SuppressLint("NewApi")
	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {
		if (myHttpResponse.getPipIndex() == NetConstant.JDCX_HOMEPAGE) {
			Document doc = myHttpResponse.getData();
			// System.out.println("查询首页:" + doc.toString());

			Elements eles = doc
					.getElementsByAttributeValue("name", "startTime").select(
							"option");
			mTermStrs = new String[eles.size()];
			for (int i = 0; i < eles.size(); i++) {
				mTermStrs[i] = eles.get(i).text();
				if (eles.get(i).hasAttr("selected")) {
					mCurSelectTermIndex = i;
				}
			}

			mTermSpinnerAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, mTermStrs);
			mTermSpinnerAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mTermStartSpinner.setAdapter(mTermSpinnerAdapter);
			mTermStartSpinner.setSelection(mCurSelectTermIndex);
			mTermEndSpinner.setAdapter(mTermSpinnerAdapter);
			mTermEndSpinner.setSelection(mCurSelectTermIndex);

			// 学生绩点获取情况
			Elements eScorepoint = doc.select("table:contains(学生绩点获取情况)")
					.select("td:contains(.)");
			String scorepoint = eScorepoint.text();
			int index;
			for (index = scorepoint.length() - 1; index >= 0; index--) {
				if (scorepoint.charAt(index) == ' ') {
					break;
				}
			}
			scorepoint = scorepoint.substring(index + 1);

			mJdTextView.setText("学生绩点获取情况：  " + scorepoint);

			// 学生各学期绩点获取情况
			eles = doc.select("table:contains(学生各学期绩点获取情况)").select("tr");
			for (Element ele : eles) {
				Elements items = ele.getElementsByTag("td");
				if (items.get(0).text().contentEquals("学生各学期绩点获取情况"))
					continue;
				if (items.get(0).text().contentEquals("学期"))
					continue;
				String[] str = new String[items.size()];
				for (int i = 0; i < items.size(); i++) {
					str[i] = items.get(i).text();
				}
				mPointListResults.add(str);
			}

			mPointListAdapter.notifyDataSetChanged();

			System.out.println("获取查询页面信息成功");
		} else if (myHttpResponse.getPipIndex() == NetConstant.JDCX_SEARCH) {

			Document doc = myHttpResponse.getData();

			Elements eScorepoint = doc.select("table:contains(学生绩点获取情况)")
					.select("td:contains(.)");
			String scorepoint = eScorepoint.text();
			int index;
			for (index = scorepoint.length() - 1; index >= 0; index--) {
				if (scorepoint.charAt(index) == ' ') {
					break;
				}
			}
			scorepoint = scorepoint.substring(index + 1);

			if (scorepoint.isEmpty()) {
				mJdTextView.setText(Messages.getString("PointSearchScreen.wrong_yearTerm_period")); //$NON-NLS-1$
			} else {
				mJdTextView.setText(Messages.getString("PointSearchScreen.student_point_in_period") + scorepoint); //$NON-NLS-1$
			}
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		MyHttpRequest req;
		switch (v.getId()) {
		case R.id.search_btn:
			params.add(new BasicNameValuePair("startTime", mTermStartSpinner
					.getSelectedItem().toString()));
			params.add(new BasicNameValuePair("endTime", mTermEndSpinner
					.getSelectedItem().toString()));
			req = new MyHttpRequest(NetConstant.TYPE_POST,
					NetConstant.URL_JDCX, params, true);
			req.setPipIndex(NetConstant.JDCX_SEARCH);
			mNetClient.sendRequest(req);
			break;
		default:
			break;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(Messages.getString("PointSearchScreen.point_search")); //$NON-NLS-1$

		setContentView(R.layout.point_search_layout);

		setSlidingMenu();

		System.out.println("cookie:" + Globe.sCookieString);
		System.out.println("进入绩点查询页面");

		mTermStartSpinner = (Spinner) findViewById(R.id.term_start_spinner);
		mTermEndSpinner = (Spinner) findViewById(R.id.term_end_spinner);
		mPointSearchBtn = (Button) findViewById(R.id.search_btn);
		mPointSearchBtn.setOnClickListener(this);
		mJdTextView = (TextView) findViewById(R.id.jd);
		mResultListView = (ListView) findViewById(R.id.score_result_list);
		mPointListAdapter = new PointListAdapter();
		mResultListView.setAdapter(mPointListAdapter);

		MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET,
				NetConstant.URL_JDCX, null, true);
		req.setPipIndex(NetConstant.JDCX_HOMEPAGE);
		mNetClient.sendRequest(req);

		mImageView = (ImageView) findViewById(R.id.menu_icon_point);
		mImageView.setImageResource(R.drawable.ic_action_location_found);
		mLinearLayout = (LinearLayout) findViewById(R.id.menu_point_search);
		mLinearLayout.setBackgroundResource(R.drawable.menu2);
		mLinearLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.showContent();
			}
		});
	}
}
