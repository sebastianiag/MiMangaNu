package ar.rulosoft.mimanganu;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.TapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

import java.io.File;
import java.util.ArrayList;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.services.DescargaListener;
import ar.rulosoft.mimanganu.services.ServicioColaDeDescarga;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.componentes.UnescroledViewPager;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.R;

public class ActivityLector extends ActionBarActivity implements DescargaListener, OnSeekBarChangeListener, TapListener {

	public static final String AJUSTE_KEY = "ajustea";

	public static DisplayType AJUSTE_PAGINA = DisplayType.FIT_TO_WIDTH;

	SectionsPagerAdapter mSectionsPagerAdapter;

	UnescroledViewPager mViewPager;
	SeekBar seekBar;
	LinearLayout seekLayout;
	Capitulo capitulo;
	ActionBar actionBar;
	UltimaPaginaFragment ultimaPaginaFragment;
	Manga manga;
	ServerBase s;
	TextView seekerPage;
	MenuItem displayMenu;
	SharedPreferences pm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pm = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		AJUSTE_PAGINA = DisplayType.valueOf(pm.getString(AJUSTE_KEY, DisplayType.FIT_TO_WIDTH.toString()));
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		actionBar = getSupportActionBar();
		actionBar.hide();
		setContentView(R.layout.activity_lector);
		capitulo = Database.getCapitulo(this, getIntent().getExtras().getInt(ActivityCapitulos.CAPITULO_ID));
		actionBar.setTitle(capitulo.getTitulo());
		manga = Database.getFullManga(this, capitulo.getMangaID());
		s = ServerBase.getServer(manga.getServerId());
		if (ServicioColaDeDescarga.actual != null)
			ServicioColaDeDescarga.actual.setDescargaListener(this);
		ultimaPaginaFragment = new UltimaPaginaFragment();
		mViewPager = (UnescroledViewPager) findViewById(R.id.pager);

		seekBar = (SeekBar) findViewById(R.id.seeker);
		seekBar.setOnSeekBarChangeListener(this);
		seekBar.setMax(capitulo.getPaginas());
		seekLayout = (LinearLayout) findViewById(R.id.seeker_layout);
		seekerPage = (TextView) findViewById(R.id.page);

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				if (arg0 < capitulo.getPaginas())
					capitulo.setPagLeidas(arg0 + 1);
				else {
					capitulo.setPagLeidas(arg0);
				}
				if (actionBar.isShowing()) {
					seekBar.setProgress(arg0);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}
		});

		capitulo.setEstadoLectura(Capitulo.LEIDO);
		Database.updateCapitulo(ActivityLector.this, capitulo);
	}

	public void actualizarIcono(DisplayType displayType) {
		if (displayMenu != null)
			switch (displayType) {
			case NONE:
				displayMenu.setIcon(R.drawable.ic_action_original);
				break;
			case FIT_TO_HEIGHT:
				displayMenu.setIcon(R.drawable.ic_action_ajustar_alto);
				break;
			case FIT_TO_WIDTH:
				displayMenu.setIcon(R.drawable.ic_action_ajustar_ancho);
				break;
			case FIT_TO_SCREEN:
				displayMenu.setIcon(R.drawable.ic_action_ajustar_diagonal);
				break;
			default:
				break;
			}
	}

	@Override
	protected void onResume() {
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mSectionsPagerAdapter);
		if (capitulo.getPagLeidas() > 1) {
			mViewPager.setCurrentItem(capitulo.getPagLeidas() - 1);
		}
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// super.onSaveInstanceState(outState);
		Database.UpdateCapituloPagina(ActivityLector.this, capitulo.getId(), capitulo.getPagLeidas());
	}

	@Override
	protected void onPause() {
		Database.UpdateCapituloPagina(ActivityLector.this, capitulo.getId(), capitulo.getPagLeidas());
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_lector, menu);
		displayMenu = menu.findItem(R.id.action_ajustar);
		actualizarIcono(AJUSTE_PAGINA);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_ajustar) {
			AJUSTE_PAGINA = AJUSTE_PAGINA.getNext();
			SharedPreferences.Editor editor = pm.edit();
			editor.putString(AJUSTE_KEY, AJUSTE_PAGINA.toString()).commit();
			mSectionsPagerAdapter.actualizarDisplayTipe();
			actualizarIcono(AJUSTE_PAGINA);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onImagenDescargada(final int cid, final int pagina) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Fragment fragment = mSectionsPagerAdapter.getIfOnMemory(pagina);
				if (fragment != null) {
					((PlaceholderFragment) fragment).new SetImagen().execute();
				}
			}
		});
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		ArrayList<PlaceholderFragment> fragments = new ArrayList<PlaceholderFragment>(5);
		int[] pos = { -1, -1, -1, -1 };
		int idx = 0;
		FragmentManager fm = null;

		private int getNextPos() {
			int np = idx % pos.length;
			idx++;
			return np;
		}

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			this.fm = fm;
			for (int i = 0; i < pos.length; i++) {
				fragments.add(new PlaceholderFragment());
			}
		}

		@Override
		public Fragment getItem(int position) {
			Fragment rsta = null;
			if (position == capitulo.getPaginas())
				rsta = ultimaPaginaFragment;
			else {
				rsta = getFragmentIn(position);
			}
			return rsta;
		}

		public Fragment getFragmentIn(int position) {
			PlaceholderFragment f = null;
			for (int i = 0; i < pos.length; i++) {
				if (pos[i] == position) {
					f = fragments.get(i);
					break;
				}
			}
			if (f == null) {
				String ruta = ServicioColaDeDescarga.generarRutaBase(s, manga, capitulo) + "/" + (position + 1) + ".jpg";
				int idx = -1;
				do {
					idx = getNextPos();
					if (pos[idx] == -1)
						break;
				} while (pos[idx] + 1 > mViewPager.getCurrentItem() && pos[idx] - 1 < mViewPager.getCurrentItem());
				pos[idx] = position;
				Fragment old = fragments.get(idx);
				fm.beginTransaction().remove(old).commit();
				old = null;
				fragments.set(idx, PlaceholderFragment.newInstance(ruta));
				f = fragments.get(idx);
				f.setTapListener(ActivityLector.this);
			}
			return f;
		}

		@Override
		public int getCount() {
			return capitulo.getPaginas() + 1;
		}

		public void actualizarDisplayTipe() {
			for (PlaceholderFragment iterable_element : fragments) {
				if (iterable_element != null) {
					iterable_element.setDisplayType(AJUSTE_PAGINA);
				}
			}
		}

		public Fragment getCurrentFragment() {
			return getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mViewPager.getCurrentItem());
		}

		public Fragment getIfOnMemory(int idx) {
			Fragment fragment = null;
			for (int i = 0; i < pos.length; i++) {
				if (pos[i] == idx) {
					fragment = fragments.get(i);
					break;
				}
			}
			return fragment;
		}

	}

	public static class PlaceholderFragment extends Fragment {

		public ImageViewTouch visor;
		ProgressBar cargando;
		TapListener mTapListener;
		Runnable r = null;

		private String ruta = null;

		private static final String RUTA = "ruta";

		public static PlaceholderFragment newInstance(String ruta) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putString(RUTA, ruta);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		public void setTapListener(TapListener nTapListener) {
			mTapListener = nTapListener;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_activity_lector_pagina, container, false);
			visor = (ImageViewTouch) rootView.findViewById(R.id.visor);
			if (r != null) {
				new Thread(r).start();
			} else
				visor.setDisplayType(AJUSTE_PAGINA);
			visor.setTapListener(mTapListener);
			visor.setScaleEnabled(false);
			cargando = (ProgressBar) rootView.findViewById(R.id.cargando);
			cargando.bringToFront();
			if (getArguments() != null)
				ruta = getArguments().getString(RUTA);

			return rootView;
		}

		public void setDisplayType(final DisplayType displayType) {
			if (visor != null) {
				visor.setDisplayType(displayType);
			} else {
				r = new Runnable() {
					@Override
					public void run() {
						visor.setDisplayType(displayType);
						r = null;
					}
				};
			}
		}

		@Override
		public void onResume() {
			visor = (ImageViewTouch) getView().findViewById(R.id.visor);
			if (visor == null) {
				cargando.setVisibility(ProgressBar.VISIBLE);
			} else if (ruta != null)
				new SetImagen().execute();

			super.onResume();
		}

		@Override
		public void onPause() {
			try {
				((BitmapDrawable) visor.getDrawable()).getBitmap().recycle();
			} catch (Exception exception) {

			}
			visor.setImageBitmap(null);
			super.onPause();
		}

		public boolean canScroll(int dx) {
			if (visor != null) {
				return visor.canScroll(dx);
			} else {
				return true;
			}
		}

		public void setImagen(String ruta) {
			this.ruta = ruta;
			new SetImagen().execute();
		}

		public class SetImagen extends AsyncTask<Void, Void, Bitmap> {

			@Override
			protected void onPreExecute() {
				if (cargando != null)
					cargando.setVisibility(ProgressBar.VISIBLE);
				super.onPreExecute();
			}

			@Override
			protected Bitmap doInBackground(Void... params) {
				Bitmap bitmap = null;
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inPreferredConfig = Config.RGB_565;
				bitmap = BitmapFactory.decodeFile(ruta, opts);
				return bitmap;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				if (result != null) {
					visor.setScaleEnabled(true);
					visor.setImageBitmap(result);
					cargando.setVisibility(ProgressBar.INVISIBLE);
				} else if (ruta != null) {
					File f = new File(ruta);
					if (f.exists()) {
						f.delete();
					}
					/*
					 * if (!ColaDeDescarga.corriendo) {
					 * ColaDeDescarga.add(((ActivityLector)
					 * getActivity()).capitulo);
					 * ColaDeDescarga.iniciarCola(getActivity()); }/
					 */
				}
				super.onPostExecute(result);
			}

		}
	}

	public static class UltimaPaginaFragment extends Fragment {

		Button b1, b2;
		Capitulo c1 = null, c2 = null;
		ActivityLector l;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rView = inflater.inflate(R.layout.fragment_pagina_final, container, false);
			b1 = (Button) rView.findViewById(R.id.button1);
			b2 = (Button) rView.findViewById(R.id.button2);
			return rView;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			l = (ActivityLector) getActivity();
			int cid = l.capitulo.getId();
			ArrayList<Capitulo> caps = l.manga.getCapitulos();
			for (int i = 0; i < caps.size(); i++) {
				if (caps.get(i).getId() == cid) {
					if (i > 0) {
						c1 = caps.get(i - 1);
					}
					if (i < caps.size() - 2) {
						c2 = caps.get(i + 1);
					}
				}
			}

			if (c1 == null) {
				b1.setVisibility(Button.INVISIBLE);
			} else {
				b1.setText(c1.getTitulo() + ">");
			}

			if (c2 == null) {
				b2.setVisibility(Button.INVISIBLE);
			} else {
				b2.setText(c2.getTitulo() + ">");
			}

			b1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new GetPaginas().execute(c1);
				}
			});

			b2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new GetPaginas().execute(c2);
				}
			});

			super.onActivityCreated(savedInstanceState);
		}

		public class GetPaginas extends AsyncTask<Capitulo, Void, Capitulo> {
			ProgressDialog asyncdialog = new ProgressDialog(getActivity());
			String error = "";

			@Override
			protected void onPreExecute() {
				asyncdialog.setMessage(getResources().getString(R.string.iniciando));
				asyncdialog.show();
				super.onPreExecute();
			}

			@Override
			protected Capitulo doInBackground(Capitulo... arg0) {
				Capitulo c = arg0[0];
				ServerBase s = ServerBase.getServer(l.manga.getServerId());
				try {
					if (c.getPaginas() < 1)
						s.iniciarCapitulo(c);
				} catch (Exception e) {
					error = e.getMessage();
					e.printStackTrace();
				} finally {
					onProgressUpdate();
				}
				return c;
			}

			@Override
			protected void onProgressUpdate(Void... values) {
				asyncdialog.dismiss();
				super.onProgressUpdate(values);
			}

			@Override
			protected void onPostExecute(Capitulo result) {
				if (error.length() > 1) {
					Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
				} else {
					asyncdialog.dismiss();
					Database.updateCapitulo(getActivity(), result);
					ServicioColaDeDescarga.agregarDescarga(getActivity(), result, true);
					// ColaDeDescarga.add(result);
					// .iniciarCola(getActivity());
					Intent intent = new Intent(getActivity(), ActivityLector.class);
					intent.putExtra(ActivityCapitulos.CAPITULO_ID, result.getId());
					getActivity().startActivity(intent);
					Database.updateCapitulo(l, l.capitulo);
					l.finish();
				}
				super.onPostExecute(result);
			}

		}

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (seekerPage != null)
			seekerPage.setText("" + progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		seekerPage.setText("" + mViewPager.getCurrentItem());
		seekerPage.setVisibility(SeekBar.VISIBLE);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		seekerPage.setVisibility(SeekBar.INVISIBLE);
		mViewPager.setCurrentItem(seekBar.getProgress());
	}

	@Override
	public void onCenterTap() {
		if (actionBar.isShowing()) {
			actionBar.hide();
			LayoutParams params = (LayoutParams) seekLayout.getLayoutParams();
			params.height = 0;
			seekLayout.setLayoutParams(params);
			seekLayout.setVisibility(LinearLayout.INVISIBLE);
		} else {
			actionBar.show();
			seekBar.setProgress(mViewPager.getCurrentItem());
			seekLayout.setVisibility(LinearLayout.VISIBLE);
			LayoutParams params = (LayoutParams) seekLayout.getLayoutParams();
			params.height = LayoutParams.WRAP_CONTENT;
			seekLayout.setLayoutParams(params);
		}

	}

	@Override
	public void onLeftTap() {
		int act = mViewPager.getCurrentItem();
		if (act > 0) {
			mViewPager.setCurrentItem(--act);
		}
	}

	@Override
	public void onRightTap() {
		int a = mSectionsPagerAdapter.getCount();
		int act = mViewPager.getCurrentItem();
		if (act < a) {
			mViewPager.setCurrentItem(++act);
		}

	}
}