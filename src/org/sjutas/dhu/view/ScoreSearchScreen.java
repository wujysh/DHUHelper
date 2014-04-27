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

public class ScoreSearchScreen extends WindowActivity implements
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
						R.layout.score_list_item_layout, null);
			}
			TextView class_type = (TextView) convertView
					.findViewById(R.id.class_type);
			class_type.setText(mScoreListResults.get(position)[0]);
			TextView class_id = (TextView) convertView
					.findViewById(R.id.class_id);
			class_id.setText(mScoreListResults.get(position)[1]);
			if (mScoreListResults.get(position).length > 2) {
				class_type.setText("");
				TextView class_name = (TextView) convertView
						.findViewById(R.id.class_name);
				class_name.setText(mScoreListResults.get(position)[2]);
				TextView class_credit = (TextView) convertView
						.findViewById(R.id.class_credit);
				class_credit.setText(mScoreListResults.get(position)[3]);
				if (mScoreListResults.get(position).length > 4) {
					String score = null, term = null;
					for (int i = mScoreListResults.get(position).length - 1; i >= mScoreListResults
							.get(position).length - 8; i--) {
						if (!mScoreListResults.get(position)[i].isEmpty()) {
							score = mScoreListResults.get(position)[i];
							term = mYearTermStrs[mScoreListResults
									.get(position).length - 1 - i];
							break;
						}
					}
					TextView class_score = (TextView) convertView
							.findViewById(R.id.class_score);
					TextView class_term = (TextView) convertView
							.findViewById(R.id.class_term);
					if (score != "" && term != "") {
						class_score.setText(score);
						class_term.setText(term);
					} else {
						class_score.setText("");
						class_term.setText("");
					}
				} else {
					TextView class_score = (TextView) convertView
							.findViewById(R.id.class_score);
					class_score.setText("");
					TextView class_term = (TextView) convertView
							.findViewById(R.id.class_term);
					class_term.setText("");
				}
			} else {
				TextView class_name = (TextView) convertView
						.findViewById(R.id.class_name);
				class_name.setText("");
				TextView class_credit = (TextView) convertView
						.findViewById(R.id.class_credit);
				class_credit.setText("");
				TextView class_score = (TextView) convertView
						.findViewById(R.id.class_score);
				class_score.setText("");
				TextView class_term = (TextView) convertView
						.findViewById(R.id.class_term);
				class_term.setText("");
			}

			return convertView;
		}

	}

	// private ArrayList<String[]> hide_params = new ArrayList<String[]>();

	private int mCurSelectTermIndex;
	// private int mCurSelectYearIndex;
	// private ArrayList<String[]> mInputStrs = new ArrayList<String[]>();

	// private TextView mJdTextView;

	private NetClient mNetClient = Globe.sNetClient;

	private ListView mResultListView;

	private ScoreListAdapter mScoreListAdapter;

	private ArrayList<String[]> mScoreListResults = new ArrayList<String[]>();

	private Button mScoreSearchBtn, mAllScoreBtn;
	// private Button mExSearchBtnOne, mExSearchBtnTwo;

	private ArrayAdapter<String> mTermSpinnerAdapter;

	private String[] mTermStrs;
	private String[] mYearTermStrs = { "4B", "4A", "3B", "3A", "2B", "2A",
			"1B", "1A" };

	private Spinner mTermSpinner;
	// private Spinner mYearSpinner;
	// private String[] mYearStrs;
	//
	// private ArrayAdapter<String> mYesrSpinnerAdapter;
	private ImageView mImageView;
	private LinearLayout mLinearLayout;

	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {
		if (myHttpResponse.getPipIndex() == NetConstant.CJCX_HOMEPAGE) {
			Document doc = myHttpResponse.getData();
			// System.out.println("查询首页:" + doc.toString());
			// Element ele = doc.getElementById("selYear");
			// Elements eles = ele.getAllElements();
			// mYearStrs = new String[eles.size() - 1];
			// for (int i = 1; i < eles.size(); i++) {
			// mYearStrs[i - 1] = eles.get(i).text();
			// if (eles.get(i).hasAttr("selected")) {
			// mCurSelectYearIndex = i;
			// }
			// }
			Elements eles = doc.select("option");
			mTermStrs = new String[eles.size()];
			for (int i = 0; i < eles.size(); i++) {
				mTermStrs[i] = eles.get(i).text();
				if (eles.get(i).hasAttr("selected")) {
					mCurSelectTermIndex = i;
				}
			}
			// mYesrSpinnerAdapter = new ArrayAdapter<String>(this,
			// android.R.layout.simple_spinner_item, mYearStrs);
			// mYesrSpinnerAdapter
			// .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// mYearSpinner.setAdapter(mYesrSpinnerAdapter);
			// mYearSpinner.setSelection(mCurSelectYearIndex);
			mTermSpinnerAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, mTermStrs);
			mTermSpinnerAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mTermSpinner.setAdapter(mTermSpinnerAdapter);
			mTermSpinner.setSelection(mCurSelectTermIndex);
			// Elements hidden_inputs = doc.select("input[type=hidden]");
			// for (Element hidden_eles : hidden_inputs) {
			// String[] arr = new String[] { hidden_eles.attr("name"),
			// hidden_eles.attr("value") };
			// hide_params.add(arr);
			// // System.out.println("获得的隐藏参数:" + arr[0] + "\n" + arr[1]);
			// }
			// Elements submit_inputs = doc
			// .select("input[type=button],input[type=submit]");
			// for (Element submit_ele : submit_inputs) {
			// String[] arr = new String[] { submit_ele.attr("name"),
			// submit_ele.attr("value") };
			// mInputStrs.add(arr);
			// }
			System.out.println("获取查询页面信息成功");
		}
		// else if (myHttpResponse.getPipIndex() == NetConstant.CJCX_SEARCH) {
		mScoreListResults.clear();
		Document doc = myHttpResponse.getData();

		Elements eles = doc.select("table:contains(课程类别)").select("tr");
		for (Element ele : eles) {
			Elements items = ele.getElementsByTag("td");
			if (items.get(0).text().contentEquals("课程类别"))
				continue;
			if (items.get(0).text().contentEquals("1A"))
				continue;
			String[] str = new String[items.size()];
			for (int i = 0; i < items.size(); i++) {
				str[i] = items.get(i).text();
				if (i == 1) {
					str[i] = str[i].replace(
							"(形式与政策,军事理论和学业考试所获学分不计入总学分,但为必修课程)", "");
					str[i] = str[i].replace(
							"(选课手册规定的高规格课程可以覆盖低规格课程,所获学分计入总学分)", "");
				}
				if (i == 2) {
					str[i] = str[i].replace("[不计绩点]", "*");
				}
			}
			mScoreListResults.add(str);
		}

		eles = doc.select("table:contains(社会考试成绩)").select("tr");
		String[] str = { "社会考试", " " };
		mScoreListResults.add(str);
		for (Element ele : eles) {
			Elements items = ele.getElementsByTag("td");
			if (items.isEmpty())
				continue;
			str = new String[items.size() + 1];
			str[0] = "";
			for (int i = 0; i < items.size(); i++) {
				str[i + 1] = items.get(i).text();
				if (i == 2) {
					str[i + 1] = str[i + 1].replace("考试成绩", "成绩");
				}
			}
			mScoreListResults.add(str);
		}

		mScoreListAdapter.notifyDataSetChanged();

		// Element ele = doc.getElementById("jd");
		// mJdTextView.setText(ele.text());

		// hide_params.clear();
		// Elements hidden_inputs = doc.select("input[type=hidden]");
		// for (Element hidden_eles : hidden_inputs) {
		// String[] arr = new String[] { hidden_eles.attr("name"),
		// hidden_eles.attr("value") };
		// hide_params.add(arr);
		// // System.out.println("获得的隐藏参数:" + arr[0] + "\n" + arr[1]);
		// }
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
					NetConstant.URL_CJCX, params, true);
			req.setPipIndex(NetConstant.CJCX_SEARCH);
			mNetClient.sendRequest(req);
			break;
		case R.id.all_score_btn:
			req = new MyHttpRequest(NetConstant.TYPE_GET,
					NetConstant.URL_CJCX_ALL, params, true);
			req.setPipIndex(NetConstant.CJCX_SEARCH);
			mNetClient.sendRequest(req);
			break;
		// case R.id.ex_search_btn1:
		// params.add(new BasicNameValuePair("selTerm", ""
		// + (mTermSpinner.getSelectedItemPosition() + 1)));
		// params.add(new BasicNameValuePair("selYear", mYearStrs[mYearSpinner
		// .getSelectedItemPosition()]));
		// params.add(new BasicNameValuePair(mInputStrs.get(2)[0], mInputStrs
		// .get(2)[1]));
		// // for (String[] arr : hide_params) {
		// // System.out.println("插入hiden参数:" + arr[0] + "\n" + arr[1]);
		// // params.add(new BasicNameValuePair(arr[0], arr[1]));
		// // }
		// req = new MyHttpRequest(NetConstant.TYPE_POST,
		// NetConstant.URL_CJCX, params, true);
		// req.setPipIndex(NetConstant.CJCX_SEARCH);
		// mNetClient.sendRequest(req);
		// break;
		// case R.id.ex_search_btn2:
		// params.add(new BasicNameValuePair("selTerm", ""
		// + (mTermSpinner.getSelectedItemPosition() + 1)));
		// params.add(new BasicNameValuePair("selYear", mYearStrs[mYearSpinner
		// .getSelectedItemPosition()]));
		// params.add(new BasicNameValuePair(mInputStrs.get(3)[0], mInputStrs
		// .get(3)[1]));
		// // for (String[] arr : hide_params) {
		// // System.out.println("插入hiden参数:" + arr[0] + "\n" + arr[1]);
		// // params.add(new BasicNameValuePair(arr[0], arr[1]));
		// // }
		// req = new MyHttpRequest(NetConstant.TYPE_POST,
		// NetConstant.URL_CJCX, params, true);
		// req.setPipIndex(NetConstant.CJCX_SEARCH);
		// mNetClient.sendRequest(req);
		// break;

		default:
			break;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("成绩查询");

		setContentView(R.layout.score_search_layout);

		setSlidingMenu();

		// overridePendingTransition(R.anim.slide_up_in, R.anim.slide_down_out);
		System.out.println("cookie:" + Globe.sCookieString);
		System.out.println("进入成绩查询页面");
		// mJdTextView = (TextView) findViewById(R.id.jd_text);
		// mYearSpinner = (Spinner) findViewById(R.id.year_spinner);
		mTermSpinner = (Spinner) findViewById(R.id.term_spinner);
		mScoreSearchBtn = (Button) findViewById(R.id.search_btn);
		mAllScoreBtn = (Button) findViewById(R.id.all_score_btn);
		// mExSearchBtnOne = (Button) findViewById(R.id.ex_search_btn1);
		// mExSearchBtnTwo = (Button) findViewById(R.id.ex_search_btn2);
		mScoreSearchBtn.setOnClickListener(this);
		mAllScoreBtn.setOnClickListener(this);
		// mExSearchBtnOne.setOnClickListener(this);
		// mExSearchBtnTwo.setOnClickListener(this);
		mResultListView = (ListView) findViewById(R.id.score_result_list);
		mScoreListAdapter = new ScoreListAdapter();
		mResultListView.setAdapter(mScoreListAdapter);
		MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET,
				NetConstant.URL_CJCX, null, true);
		req.setPipIndex(NetConstant.CJCX_HOMEPAGE);
		mNetClient.sendRequest(req);

		mImageView = (ImageView) findViewById(R.id.menu_icon_score);
		mImageView.setImageResource(R.drawable.ic_action_location_found);
		mLinearLayout = (LinearLayout) findViewById(R.id.menu_score_search);
		mLinearLayout.setBackgroundResource(R.drawable.menu2);
		mLinearLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menu.showContent();
			}
		});
	}
}
