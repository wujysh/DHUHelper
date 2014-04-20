package com.wujy.dhuhelper.view;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.wujy.dhuhelper.Globe;
import com.wujy.dhuhelper.R;
import com.wujy.dhuhelper.WindowActivity;
import com.wujy.dhuhelper.net.MyHttpRequest;
import com.wujy.dhuhelper.net.MyHttpResponse;
import com.wujy.dhuhelper.net.NetConstant;

public class NewsScreen extends WindowActivity {
	private class ListViewAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (listtype == 0)
				return Globe.mFirstListItems.size();
			else if (listtype == 1)
				return Globe.mSecondListItems.size();
			else if (listtype == 2)
				return Globe.mThirdListItems.size();
			return 0;
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
						R.layout.homepage_listview_layout, null);
			}
			if (listtype == 0) {
				TextView name = (TextView) convertView.findViewById(R.id.name);
				name.setText(Globe.mFirstListItems.get(position)[1]);
				TextView time = (TextView) convertView.findViewById(R.id.time);
				time.setText(Globe.mFirstListItems.get(position)[2]);
			} else if (listtype == 1) {
				TextView name = (TextView) convertView.findViewById(R.id.name);
				name.setText(Globe.mSecondListItems.get(position)[1]);
				TextView time = (TextView) convertView.findViewById(R.id.time);
				time.setText(Globe.mSecondListItems.get(position)[2]);
			} else if (listtype == 2) {
				TextView name = (TextView) convertView.findViewById(R.id.name);
				name.setText(Globe.mThirdListItems.get(position)[1]);
				TextView time = (TextView) convertView.findViewById(R.id.time);
				time.setText(Globe.mThirdListItems.get(position)[2]);
			}

			return convertView;
		}

	}

	public static String MAIN_URL_JW = "http://jw.dhu.edu.cn/dhu/news/";
	public static String MAIN_URL_DHU = "http://www2.dhu.edu.cn/dhuxxxt/xinwenwang/";

	private int listtype = 0;// 默认为东华新闻

	private ListViewAdapter mAdapter;

	private ListView mListView;
	private TextView mTextView;
	private RadioGroup mRadioGroup;
	private RadioButton mRadioButton1, mRadioButton2, mRadioButton3;
	private ImageView mImageView1, mImageView2, mImageView3;
	private LinearLayout mLinearLayout1, mLinearLayout2, mLinearLayout3;

	@Override
	public void handResponse(MyHttpResponse myHttpResponse) {
		if (myHttpResponse.getPipIndex() == NetConstant.NEWS_DHXW) {
			Globe.mFirstListItems.clear();

			Document doc = myHttpResponse.getData();
			Elements tables = doc.select("table[width=96%]");
			Elements items = tables.select("td[width=96%]");
			for (Element item : items) {
				String[] arr = new String[3];
				arr[0] = MAIN_URL_DHU + item.select("a").attr("href");
				arr[1] = item.select("a").text();
				arr[2] = item.text().substring(
						item.text().lastIndexOf("(") + 1,
						item.text().length() - 1);
				Globe.mFirstListItems.add(arr);
			}

			mAdapter.notifyDataSetChanged();
			mTextView.setVisibility(View.GONE);
		} else if (myHttpResponse.getPipIndex() == NetConstant.NEWS_XNGG) {
			Globe.mSecondListItems.clear();

			Document doc = myHttpResponse.getData();
			Elements tables = doc.select("table[width=96%]");
			Elements items = tables.select("td[width=96%]");
			for (Element item : items) {
				String[] arr = new String[3];
				arr[0] = MAIN_URL_DHU + item.select("a").attr("href");
				arr[1] = item.select("a").text();
				arr[2] = item.text().substring(
						item.text().lastIndexOf("(") + 1,
						item.text().length() - 1);
				Globe.mSecondListItems.add(arr);
			}

			mAdapter.notifyDataSetChanged();
			mTextView.setVisibility(View.GONE);
		} else if (myHttpResponse.getPipIndex() == NetConstant.NEWS_JWXX) {
			Globe.mThirdListItems.clear();

			Document doc = myHttpResponse.getData();
			Elements tables = doc.select("table[id=table1]");
			Elements items = tables.select("tr[align=center]");
			for (Element item : items) {
				String[] arr = new String[3];
				arr[0] = item.select("a").attr("href");
				arr[1] = item.select("a").text();
				arr[2] = item.text().substring(item.text().length() - 10);
				Globe.mThirdListItems.add(arr);
			}

			mAdapter.notifyDataSetChanged();
			mTextView.setVisibility(View.GONE);
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
		setTitle("新闻信息");

		setContentView(R.layout.activity_main);

		setSlidingMenu();
		
		mListView = (ListView) findViewById(R.id.homepage_listview);
		mAdapter = new ListViewAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent it = new Intent(NewsScreen.this, WebViewScreen.class);
				if (listtype == 0) {
					it.putExtra("name", Globe.mFirstListItems.get(position)[1]);
					it.putExtra("url", Globe.mFirstListItems.get(position)[0]);
				} else if (listtype == 1) {
					it.putExtra("name", Globe.mSecondListItems.get(position)[1]);
					it.putExtra("url", Globe.mSecondListItems.get(position)[0]);
				} else if (listtype == 2) {
					it.putExtra("name", Globe.mThirdListItems.get(position)[1]);
					it.putExtra("url", Globe.mThirdListItems.get(position)[0]);
				}
				startActivity(it);
			}
		});
		
		mTextView = (TextView) findViewById(R.id.homepage_load_news_hint);
		if (Globe.news_load_when_startup) {
			mTextView.setText(Messages.getString("NewsScreen.loading_please_wait")); //$NON-NLS-1$
			mTextView.setVisibility(View.GONE);
		} else {
			mTextView.setText(Messages.getString("NewsScreen.check_button_group_to_load_news")); //$NON-NLS-1$
			
		}

		mLinearLayout1 = (LinearLayout) findViewById(R.id.menu_dhxw);
		mLinearLayout2 = (LinearLayout) findViewById(R.id.menu_xngg);
		mLinearLayout3 = (LinearLayout) findViewById(R.id.menu_jwxx);
		mLinearLayout1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				listtype = 0;
				// mRadioGroup.check(R.id.DHXW);
				mRadioButton1.setChecked(true);
				menu.showContent();
			}
		});
		mLinearLayout2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				listtype = 1;
				// mRadioGroup.check(R.id.XNGG);
				mRadioButton2.setChecked(true);
				menu.showContent();
			}
		});
		mLinearLayout3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				listtype = 2;
				// mRadioGroup.check(R.id.JWXX);
				mRadioButton3.setChecked(true);
				menu.showContent();
			}
		});

		mRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
		mRadioButton1 = (RadioButton) findViewById(R.id.DHXW);
		mRadioButton2 = (RadioButton) findViewById(R.id.XNGG);
		mRadioButton3 = (RadioButton) findViewById(R.id.JWXX);
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.DHXW:
					// System.out.println("RadioButton1 checked invoked");
					if (Globe.mFirstListItems.isEmpty()) {
						mTextView.setText(Messages.getString("NewsScreen.loading_please_wait")); //$NON-NLS-1$
						mTextView.setVisibility(View.VISIBLE);
						prepareNetClient();
						MyHttpRequest req = new MyHttpRequest(
								NetConstant.TYPE_GET,
								NetConstant.URL_NEWS_DHXW, null, true);
						req.setPipIndex(NetConstant.NEWS_DHXW);
						mNetClient.sendRequest(req);
					}
					listtype = 0;
					mImageView1
							.setImageResource(R.drawable.ic_action_location_found);
					mImageView2
							.setImageResource(R.drawable.ic_action_location_searching);
					mImageView3
							.setImageResource(R.drawable.ic_action_location_searching);
					mLinearLayout1.setBackgroundResource(R.drawable.menu2);
					mLinearLayout2.setBackgroundResource(R.drawable.menu);
					mLinearLayout3.setBackgroundResource(R.drawable.menu);
					break;
				case R.id.XNGG:
					// System.out.println("RadioButton2 checked invoked");
					if (Globe.mSecondListItems.isEmpty()) {
						mTextView.setText(Messages.getString("NewsScreen.loading_please_wait")); //$NON-NLS-1$
						mTextView.setVisibility(View.VISIBLE);
						prepareNetClient();
						MyHttpRequest req = new MyHttpRequest(
								NetConstant.TYPE_GET,
								NetConstant.URL_NEWS_XNGG, null, true);
						req.setPipIndex(NetConstant.NEWS_XNGG);
						mNetClient.sendRequest(req);
					}
					listtype = 1;
					mImageView2
							.setImageResource(R.drawable.ic_action_location_found);
					mImageView1
							.setImageResource(R.drawable.ic_action_location_searching);
					mImageView3
							.setImageResource(R.drawable.ic_action_location_searching);
					mLinearLayout2.setBackgroundResource(R.drawable.menu2);
					mLinearLayout1.setBackgroundResource(R.drawable.menu);
					mLinearLayout3.setBackgroundResource(R.drawable.menu);
					break;
				case R.id.JWXX:
					// System.out.println("RadioButton3 checked invoked");
					if (Globe.mThirdListItems.isEmpty()) {
						mTextView.setText(Messages.getString("NewsScreen.loading_please_wait")); //$NON-NLS-1$
						mTextView.setVisibility(View.VISIBLE);
						prepareNetClient();
						MyHttpRequest req = new MyHttpRequest(
								NetConstant.TYPE_GET,
								NetConstant.URL_NEWS_JWXX, null, true);
						req.setPipIndex(NetConstant.NEWS_JWXX);
						mNetClient.sendRequest(req);
					}
					listtype = 2;
					mImageView3
							.setImageResource(R.drawable.ic_action_location_found);
					mImageView1
							.setImageResource(R.drawable.ic_action_location_searching);
					mImageView2
							.setImageResource(R.drawable.ic_action_location_searching);
					mLinearLayout3.setBackgroundResource(R.drawable.menu2);
					mLinearLayout1.setBackgroundResource(R.drawable.menu);
					mLinearLayout2.setBackgroundResource(R.drawable.menu);
					break;
				default:
					break;
				}
				mAdapter.notifyDataSetChanged();
			}
		});

		mImageView1 = (ImageView) findViewById(R.id.menu_icon_dhxw);
		mImageView2 = (ImageView) findViewById(R.id.menu_icon_xngg);
		mImageView3 = (ImageView) findViewById(R.id.menu_icon_jwxx);

		char RadioChecked = getIntent().getCharExtra("RadioChecked", '0');
		if (RadioChecked == '0') {
			listtype = 0;

			// MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET,
			// NetConstant.URL_NEWS_DHXW, null, true);
			// req.setPipIndex(NetConstant.NEWS_DHXW);
			// mNetClient.sendRequest(req);

			mRadioButton1.setChecked(true);
			mImageView1.setImageResource(R.drawable.ic_action_location_found);
			mLinearLayout1.setBackgroundResource(R.drawable.menu2);
			mAdapter.notifyDataSetChanged();
			// mRadioGroup.check(R.id.DHXW);
		} else if (RadioChecked == '1') {
			listtype = 1;

			// MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET,
			// NetConstant.URL_NEWS_XNGG, null, true);
			// req.setPipIndex(NetConstant.NEWS_XNGG);
			// mNetClient.sendRequest(req);

			mRadioButton2.setChecked(true);
			// mRadioGroup.check(R.id.XNGG);
		} else if (RadioChecked == '2') {
			listtype = 2;

			// MyHttpRequest req = new MyHttpRequest(NetConstant.TYPE_GET,
			// NetConstant.URL_NEWS_JWXX, null, true);
			// req.setPipIndex(NetConstant.NEWS_JWXX);
			// mNetClient.sendRequest(req);

			mRadioButton3.setChecked(true);
			// mRadioGroup.check(R.id.JWXX);
		}
		
		if (mListView.getCount() != 0) mTextView.setVisibility(View.GONE);
	}

}
