package ar.rulosoft.mimanganu;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import ar.rulosoft.mimanganu.adapters.DescargaAdapter;
import ar.rulosoft.mimanganu.services.DescargaCapitulo;
import ar.rulosoft.mimanganu.services.ServicioColaDeDescarga;
import ar.rulosoft.mimanganu.R;

public class FragmentDescarga extends Fragment {

	ListView lista;
	MostrarDescargas md;
	DescargaAdapter adap;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rView = inflater.inflate(R.layout.fragment_descargas, container, false);
		lista = (ListView) rView.findViewById(R.id.descargas);
		return rView;
	}

	@SuppressLint("NewApi")
	@Override
	public void onResume() {
		adap = new DescargaAdapter(getActivity(), new ArrayList<DescargaCapitulo>(), (ActivityCapitulos) getActivity());
		lista.setAdapter(adap);
		md = new MostrarDescargas();
		if (Build.VERSION.SDK_INT >= 11)
			md.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		else
			md.execute();
		super.onResume();
	}

	@Override
	public void onPause() {
		md.stop();
		super.onPause();
	}

	private class MostrarDescargas extends AsyncTask<Void, Void, Void> {
		boolean seguir = true;

		@Override
		protected Void doInBackground(Void... params) {
			while (seguir) {
				try {
					adap.updateAll(ServicioColaDeDescarga.descargas);
					publishProgress();
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					adap.notifyDataSetChanged();
				}
			});
			super.onProgressUpdate(values);
		}

		public void stop() {
			seguir = false;
		}

	}
}
