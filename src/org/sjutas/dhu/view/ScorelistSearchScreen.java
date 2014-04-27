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

public class ScorelistSearchScreen extends WindowActivity implements
		OnClickListener {
	private class ScoreListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mScoreListResults.size();
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
						R.layout.scorelist_list_item_layout, null);
			}

			TextView course_name = (TextView) convertView
					.findViewById(R.id.course_name);
			course_name.setText(mScoreListResults.get(position)[0]);
			TextView course_credit = (TextView) convertView
					.findViewById(R.id.course_credit);
			course_credit.setText(mScoreListResults.get(position)[1]);
			TextView course_type = (TextView) convertView
					.findViewById(R.id.course_type);
			course_type.setText(mScoreListResults.get(position)[2]);
			TextView course_score = (TextView) convertView
					.findViewById(R.id.course_score);
			course_score.setText(mScoreListResults.get(position)[3]);
			TextView course_isCredit = (TextView) convertView
					.findViewById(R.id.course_isCredit);
			course_isCredit.setText(mScoreListResults.get(position)[4]);
			TextView course_isPoint = (TextView) convertView
					.findViewById(R.id.course_isPoint);
			course_isPoint.setText(mScoreListResults.get(position)[5]);
			TextView course_others = (TextView) convertView
					.findViewById(R.id.course_others);
			course_others.setText(mScoreListResults.get(position)[6]);

			return convertView;
		}

	}

	private int mCurSelectTermIndex;

	private NetClient mNetClient = Globe.sNetClient;

	private ListView mResultListView;

	private ScoreListAdapter mScoreListAdapter;

	private ArrayList<String[]> mScoreListResults = new ArrayList<String[]>();

	private Button mScoreSearchBtn, mCurrentScoreSearchBtn;

	private ArrayAdapter<String> mTermSpinnerAdapter;

	private String[] mTermStrs;

	private Spinner mTermSpinner;

	private ImageView mImageView;
	private LinearLayout mLinearLayout;

	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {
		if (myHttpResponse.getPipIndex() == NetConstant.CJDCX_HOMEPAGE) {
			Document doc = myHttpResponse.getData();
			// System.out.println("查询首页:" + doc.toString());

			Elements eles = doc.select("option");
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
			mTermSpinner.setAdapter(mTermSpinnerAdapter);
			mTermSpinner.setSelection(mCurSelectTermIndex);

			System.out.println("获取查询页面信息成功");
		}
		// else if (myHttpResponse.getPipIndex() == NetConstant.CJDCX_SEARCH) {
		mScoreListResults.clear();
		Document doc = myHttpResponse.getData();

		Elements eles = doc.select("table:contains(课程名称)").select("tr");
		for (Element ele : eles) {
			Elements items = ele.getElementsByTag("td");
			if (items.get(0).text().contentEquals("课程名称"))
				continue;
			if (items.get(0).text().contentEquals("学业警告"))
				break;
			String[] str = new String[items.size()];
			for (int i = 0; i < items.size(); i++) {
				str[i] = items.get(i).text();
			}
			mScoreListResults.add(str);
		}

		mScoreListAdapter.notifyDataSetChanged();

		// System.out.println(doc.toString());
		// }
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		MyHttpRequest req;
		switch (v.getId()) {
		case R.id.search_btn:
			params.add(new BasicNameValuePair("yearTerm", mTermSpinner
					.getSelectedItem().toString()));
			req = new MyHttpRequest(NetConstant.TYPE_POST,
					NetConstant.URL_CJDCX, params, true);
			req.setPipIndex(NetConstant.CJDCX_SEARCH);
			mNetClient.sendRequest(req);
			break;
		case R.id.current_score_btn:
			// params.add(new BasicNameValuePair("yearTerm",
			// mTermSpinner.getSelectedItem().toString()));
			req = new MyHttpRequest(NetConstant.TYPE_GET,
					NetConstant.URL_CJDCX, params, true);
			req.setPipIndex(NetConstant.CJDCX_SEARCH);
			mNetClient.sendRequest(req);
			break;
		default:
			break;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("学生个人成绩单");

		setContentView(R.layout.scorelist_search_layout);

		setSlidingMenu();

		System.out.println("cookie:" + Globe.sCookieString);
		System.out.println("进入成绩单查询页面");

		mTermSpinner = (Spinner) findViewById(R.id.term_spinner);
		mScoreSearchBtn = (Button) findViewById(R.id.search_btn);
		mScoreSearchBtn.setOnClickListener(this);
		mCurrentScoreSearchBtn = (Button) findViewById(R.id.current_score_btn);
		mCurrentScoreSearchBtn.setOnClickListener(this);
		mResultListView = (ListView) findViewById(R.id.score_result_list);
		mScoreListAdapter = new ScoreListAdapter();
		mResultListView.setAdapter(mScoreListAdapter);

		MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET,
				NetConstant.URL_CJDCX, null, true);
		req.setPipIndex(NetConstant.CJDCX_HOMEPAGE);
		mNetClient.sendRequest(req);

		mImageView = (ImageView) findViewById(R.id.menu_icon_scorelist);
		mImageView.setImageResource(R.drawable.ic_action_location_found);
		mLinearLayout = (LinearLayout) findViewById(R.id.menu_scorelist_search);
		mLinearLayout.setBackgroundResource(R.drawable.menu2);
		mLinearLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.showContent();
			}
		});
	}
}
