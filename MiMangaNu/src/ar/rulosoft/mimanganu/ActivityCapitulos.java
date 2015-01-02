package ar.rulosoft.mimanganu;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.R;

public class ActivityCapitulos extends ActionBarActivity {

	public static final String CAPITULO_ID = "cap_id";

	SectionsPagerAdapter mSectionsPagerAdapter;
	PagerTabStrip pagerStrip;
	FragmentCapitulos fragmentCapitulos;
	FragmentDetalles fragmentDetalles;
	FragmentDescarga fragmentDescarga;
	ViewPager mViewPager;
	SetCapitulos listenerCapitulos;
	public Manga manga;
	int id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity_capitulos);
		
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		fragmentCapitulos = new FragmentCapitulos();
		fragmentCapitulos.setRetainInstance(true);
		listenerCapitulos = fragmentCapitulos;

		fragmentDetalles = new FragmentDetalles();
		fragmentDetalles.setRetainInstance(true);

		fragmentDescarga = new FragmentDescarga();

		mSectionsPagerAdapter.add(fragmentCapitulos);
		mSectionsPagerAdapter.add(fragmentDetalles);
		mSectionsPagerAdapter.add(fragmentDescarga);

		id = getIntent().getExtras().getInt(ActivityMisMangas.MANGA_ID, -1);
		if (id == -1) {
			onBackPressed();
			finish();
		}
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_strip);
		pagerTabStrip.setDrawFullUnderline(true);
		pagerTabStrip.setTabIndicatorColor(Color.BLACK);
	}

	@Override
	protected void onResume() {
		super.onResume();
		manga = Database.getFullManga(getApplicationContext(), id);
		listenerCapitulos.onCalpitulosCargados(this, manga.getCapitulos());
		fragmentDetalles.m = manga;
		Database.updateMangaLeido(this, manga.getId());
		Database.updateMangaNuevos(ActivityCapitulos.this, manga, -100);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_capitulos, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.descargar_restantes) {
			ArrayList<Capitulo> capitulos = Database.getCapitulos(ActivityCapitulos.this, ActivityCapitulos.this.id, Database.COL_CAP_DESCARGADO + " != 1");
			Capitulo[] arr = new Capitulo[capitulos.size()];
			arr = capitulos.toArray(arr);
			new DascargarDemas().execute(arr);

			// TODO mecanimos

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		List<Fragment> fragments;
		private String tabs[] = new String[] { getResources().getString(R.string.capitulos), getResources().getString(R.string.info),
				getResources().getString(R.string.descargas) };

		public void add(Fragment f) {
			fragments.add(f);
		}

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			this.fragments = new ArrayList<Fragment>();
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public float getPageWidth(int position) {
			float size = 1f;
			if (position == 0)
				size = 0.9f;
			return size;
		}

		public CharSequence getPageTitle(int position) {
			return tabs[position];
		}

	}

	public interface SetCapitulos {
		void onCalpitulosCargados(Activity c, ArrayList<Capitulo> capitulos);
	}

	public class DascargarDemas extends AsyncTask<Capitulo, Void, Void> {
		private ServerBase server;
		private Context context;

		@Override
		protected void onPreExecute() {
			server = ServerBase.getServer(ActivityCapitulos.this.manga.getServerId());
			context = getApplicationContext();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Capitulo... capitulos) {
			for (Capitulo c : capitulos) {
				try {
					server.iniciarCapitulo(c);
					Database.updateCapitulo(context, c);
					ColaDeDescarga.addCola(c);
					ColaDeDescarga.iniciarCola(context);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

	}
}
