package ar.rulosoft.mimanganu;

import com.fedorvlasov.lazylist.ImageLoader;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.DatosSerie;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.R;

public class ActivityDetalles extends ActionBarActivity {

	public static final String TITULO = "titulo_m";
	public static final String PATH = "path_m";

	ImageLoader imageLoader;
	DatosSerie datos;
	ProgressBar cargando;
	ServerBase s;
	Manga m;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detalles);
		datos = (DatosSerie) findViewById(R.id.datos);
		cargando = (ProgressBar) findViewById(R.id.cargando);
		String titulo = getIntent().getExtras().getString(TITULO);
		getSupportActionBar().setTitle(getResources().getString(R.string.datosde) + titulo);
		String path = getIntent().getExtras().getString(PATH);
		int id = getIntent().getExtras().getInt(ActivityMisMangas.SERVER_ID);
		m = new Manga(id, titulo, path, false);
		s = ServerBase.getServer(id);
		imageLoader = new ImageLoader(this.getApplicationContext());
		new CargarDetalles().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_manga, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_add) {
			new AgregaManga().execute(m);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class CargarDetalles extends AsyncTask<Void, Void, Void> {
		
		String error = ".";
		
		@Override
		protected void onPreExecute() {
			cargando.setVisibility(ProgressBar.VISIBLE);
			cargando.bringToFront();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				s.cargarPortada(m);
			} catch (Exception e) {
				error = e.getMessage();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			cargando.setVisibility(ProgressBar.INVISIBLE);
			datos.pTitle.setColor(Color.BLACK);
			datos.pTxt.setColor(Color.BLACK);
			datos.inicializar(m.getTitulo(), m.getSinopsis(), 166, 250);
			imageLoader.DisplayImage(m.getImages(), datos);
			if(error != null && error.length() > 2){
				Toast.makeText(ActivityDetalles.this, error, Toast.LENGTH_LONG).show();
			}
		}

	}

	public class AgregaManga extends AsyncTask<Manga, Void, Void> {
		ProgressDialog agregando = new ProgressDialog(ActivityDetalles.this);
		String error = ".";

		@Override
		protected void onPreExecute() {
			agregando.setMessage(getResources().getString(R.string.agregando));
			agregando.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Manga... params) {
			try {
				s.cargarCapitulos(m);
			} catch (Exception e) {
				error = e.getMessage();
			}
			Database.addManga(getBaseContext(), params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			agregando.dismiss();
			Toast.makeText(ActivityDetalles.this, getResources().getString(R.string.agregado), Toast.LENGTH_SHORT).show();
			if(error != null && error.length() > 2){
				Toast.makeText(ActivityDetalles.this, error, Toast.LENGTH_LONG).show();
			}
			onBackPressed();
			super.onPostExecute(result);
		}

	}
}
