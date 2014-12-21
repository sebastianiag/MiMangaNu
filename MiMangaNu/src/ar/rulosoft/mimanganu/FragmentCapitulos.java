package ar.rulosoft.mimanganu;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import ar.rulosoft.mimanganu.ActivityCapitulos.SetCapitulos;
import ar.rulosoft.mimanganu.adapters.CapituloAdapter;
import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.R;

public class FragmentCapitulos extends Fragment implements SetCapitulos {

	ListView lista;
	CapituloAdapter capitulosAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rView = inflater.inflate(R.layout.fragment_capitulos, container, false);
		lista = (ListView) rView.findViewById(R.id.lista);
		return rView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		lista.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Capitulo c = (Capitulo) lista.getAdapter().getItem(position);
				new GetPaginas().execute(c);
			}
		});

		registerForContextMenu(lista);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		lista.setAdapter(capitulosAdapter);
		lista.setSelection(((ActivityCapitulos) getActivity()).manga.getLastIndex());
	}

	@Override
	public void onPause() {
		int first = lista.getFirstVisiblePosition();
		Database.updateMangaLastIndex(getActivity(), ((ActivityCapitulos) getActivity()).manga.getId(), first);
		super.onPause();
	}

	@Override
	public void onCalpitulosCargados(Activity c, ArrayList<Capitulo> capitulos) {
		capitulosAdapter = new CapituloAdapter(c, capitulos);
		if (lista != null) {
			lista.setAdapter(capitulosAdapter);
			lista.setSelection(((ActivityCapitulos) getActivity()).manga.getLastIndex());
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		MenuInflater inflater = getActivity().getMenuInflater();

		if (v.getId() == R.id.lista)
			;//TODO
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		menu.setHeaderTitle(lista.getAdapter().getItem(info.position).toString());
		inflater.inflate(R.menu.listitem_capitulo_menu, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		if (item.getItemId() == R.id.borrar) {
			Capitulo c = (Capitulo) lista.getAdapter().getItem(info.position);
			Manga manga = ((ActivityCapitulos) getActivity()).manga;
			ServerBase s = ServerBase.getServer(manga.getServerId());
			String ruta = ColaDeDescarga.generarRutaBase(s, manga, c);
			FragmentMisMangas.DeleteRecursive(new File(ruta));
			Database.borrarCapitulo(getActivity(), c);
			capitulosAdapter.remove(c);
			capitulosAdapter.notifyDataSetChanged();
		} else if (item.getItemId() == R.id.reset) {
			Capitulo c = (Capitulo) lista.getAdapter().getItem(info.position);
			Manga manga = ((ActivityCapitulos) getActivity()).manga;
			ServerBase s = ServerBase.getServer(manga.getServerId());
			String ruta = ColaDeDescarga.generarRutaBase(s, manga, c);
			FragmentMisMangas.DeleteRecursive(new File(ruta));
			Database.borrarCapitulo(getActivity(), c);
			c.setPaginas(0);
			c.setDescargado(false);
			c.setPagLeidas(0);
			Database.updateCapitulo(getActivity(), c);
			Database.UpdateCapituloDescargado(getActivity(), c.getId(), 0);
			capitulosAdapter.notifyDataSetChanged();
		}
		return super.onContextItemSelected(item);
	}

	private class GetPaginas extends AsyncTask<Capitulo, Void, Capitulo> {
		ProgressDialog asyncdialog = new ProgressDialog(getActivity());
		String error = "";

		@Override
		protected void onPreExecute() {
			asyncdialog.setMessage(getResources().getString(R.string.iniciando));
			asyncdialog.show();
		}

		@Override
		protected Capitulo doInBackground(Capitulo... arg0) {
			Capitulo c = arg0[0];
			ServerBase s = ServerBase.getServer(((ActivityCapitulos) getActivity()).manga.getServerId());
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
				ColaDeDescarga.add(result);
				ColaDeDescarga.iniciarCola(getActivity());
				int firs = lista.getFirstVisiblePosition();
				Database.updateMangaLastIndex(getActivity(), ((ActivityCapitulos) getActivity()).manga.getId(), firs);
				Intent intent = new Intent(getActivity(), ActivityLector.class);
				intent.putExtra(ActivityCapitulos.CAPITULO_ID, result.getId());
				getActivity().startActivity(intent);
			}
			super.onPostExecute(result);
		}
	}
}
