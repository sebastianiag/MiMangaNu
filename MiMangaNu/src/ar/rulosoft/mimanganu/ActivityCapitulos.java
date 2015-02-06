package ar.rulosoft.mimanganu;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import ar.rulosoft.mimanganu.services.ServicioColaDeDescarga;
import ar.rulosoft.mimanganu.R;

public class ActivityCapitulos extends ActionBarActivity {

	public static enum Direccion {L2R, R2L, VERTICAL};
	public static final String DIRECCION = "direccion_de_lectura";


	public static final String CAPITULO_ID = "cap_id";

	SectionsPagerAdapter mSectionsPagerAdapter;
	PagerTabStrip pagerStrip;
	FragmentCapitulos fragmentCapitulos;
	FragmentDetalles fragmentDetalles;
	FragmentDescarga fragmentDescarga;
	SharedPreferences pm;
	ViewPager mViewPager;
	MenuItem sentido;
	SetCapitulos listenerCapitulos;
	public Manga manga;
	public Direccion direccion;
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

		pm = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}

	@Override
	protected void onResume() {
		super.onResume();
		manga = Database.getFullManga(getApplicationContext(), id);
		listenerCapitulos.onCalpitulosCargados(this, manga.getCapitulos());
		fragmentDetalles.m = manga;
		Database.updateMangaLeido(this, manga.getId());
		Database.updateMangaNuevos(ActivityCapitulos.this, manga, -100);
		BuscarNuevo.onActivityResumed(ActivityCapitulos.this);
	}

	@Override
	protected void onPause() {
		BuscarNuevo.onActivityPaused();
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_capitulos, menu);
		sentido = menu.findItem(R.id.action_sentido);
		int direccion = pm.getInt(DIRECCION, Direccion.R2L.ordinal());
		if(direccion == Direccion.R2L.ordinal()){
			this.direccion = Direccion.R2L;
			sentido.setIcon(R.drawable.ic_action_clasico);
		}else if(direccion == Direccion.L2R.ordinal()){
			this.direccion = Direccion.L2R;
			sentido.setIcon(R.drawable.ic_action_inverso);
		}else{
			this.direccion = Direccion.VERTICAL;
			sentido.setIcon(R.drawable.ic_action_verical);
		}
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.action_descargar_restantes) {
			ArrayList<Capitulo> capitulos = Database.getCapitulos(ActivityCapitulos.this, ActivityCapitulos.this.id, Database.COL_CAP_DESCARGADO + " != 1");
			Capitulo[] arr = new Capitulo[capitulos.size()];
			arr = capitulos.toArray(arr);
			new DascargarDemas().execute(arr);
			// TODO mecanimos
			return true;
		} else if (id == R.id.action_marcar_todo_leido) {
			Database.marcarTodoComoLeido(ActivityCapitulos.this, this.id);
			fragmentCapitulos.onCalpitulosCargados(ActivityCapitulos.this, Database.getCapitulos(ActivityCapitulos.this, this.id));
		} else if (id == R.id.action_marcar_todo_no_leido) {
			Database.marcarTodoComoNoLeido(ActivityCapitulos.this, this.id);
			fragmentCapitulos.onCalpitulosCargados(ActivityCapitulos.this, Database.getCapitulos(ActivityCapitulos.this, this.id));
		} else if (id == R.id.action_buscarnuevos) {
			new BuscarNuevo().setActivity(ActivityCapitulos.this).execute(manga);
		} else if (id == R.id.action_sentido) {
			int direccion = pm.getInt(DIRECCION, Direccion.R2L.ordinal());
			if(direccion == Direccion.R2L.ordinal()){
				sentido.setIcon(R.drawable.ic_action_inverso);
				pm.edit().putInt(DIRECCION, Direccion.L2R.ordinal()).commit();
				this.direccion = Direccion.L2R;
			}else if(direccion == Direccion.L2R.ordinal()){
				sentido.setIcon(R.drawable.ic_action_verical);
				pm.edit().putInt(DIRECCION, Direccion.VERTICAL.ordinal()).commit();
				this.direccion = Direccion.VERTICAL;
			}else{
				sentido.setIcon(R.drawable.ic_action_clasico);
				pm.edit().putInt(DIRECCION, Direccion.R2L.ordinal()).commit();
				this.direccion = Direccion.R2L;
			}
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
					ServicioColaDeDescarga.agregarDescarga(ActivityCapitulos.this, c, false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

	}

	public static class BuscarNuevo extends AsyncTask<Manga, String, Integer> {

		Activity activity;
		ProgressDialog progreso;
		static boolean running = false;
		static BuscarNuevo actual = null;
		int mangaId = 0;
		String msg;

		public static void onActivityPaused() {
			if (running && actual.progreso != null)
				actual.progreso.dismiss();
		}

		public static void onActivityResumed(Activity actvt) {
			if (running && actual != null) {
				actual.progreso = new ProgressDialog(actvt);
				actual.progreso.setCancelable(false);
				actual.progreso.setMessage(actual.msg);
				actual.progreso.show();
			}
		}

		public BuscarNuevo setActivity(Activity activity) {
			this.activity = activity;
			return this;
		}

		@Override
		protected void onPreExecute() {
			running = true;
			actual = this;
			progreso = new ProgressDialog(activity);
			progreso.setCancelable(false);
			msg = activity.getResources().getString(R.string.buscandonuevo);
			progreso.setTitle(msg);
			progreso.show();
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(String... values) {
			final String s = values[0];
			msg = s;
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					progreso.setMessage(s);
				}
			});
			super.onProgressUpdate(values);
		}

		@Override
		protected Integer doInBackground(Manga... params) {
			int result = 0;
			Database.removerCapitulosHuerfanos(activity);
			ServerBase s = ServerBase.getServer(params[0].getServerId());
			mangaId = params[0].getId();
			try {
				onProgressUpdate(params[0].getTitulo());
				params[0].setCapitulos(null);
				s.cargarCapitulos(params[0]);
				int diff = s.buscarNuevosCapitulos(params[0], activity);
				result += diff;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (((ActivityCapitulos) activity).fragmentCapitulos != null && result > 0)
				((ActivityCapitulos) activity).fragmentCapitulos.onCalpitulosCargados(activity, Database.getCapitulos(activity, mangaId));
			if (progreso != null && progreso.isShowing()) {
				try {
					progreso.dismiss();
				} catch (Exception e) {

				}
			}
			running = false;
			actual = null;
		}
	}
}
