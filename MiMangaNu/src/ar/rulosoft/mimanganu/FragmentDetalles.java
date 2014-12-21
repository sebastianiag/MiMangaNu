package ar.rulosoft.mimanganu;

import com.fedorvlasov.lazylist.ImageLoader;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ar.rulosoft.mimanganu.componentes.DatosSerie;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.R;

public class FragmentDetalles extends Fragment {

	Manga m;
	ImageLoader imageLoader;
	DatosSerie datos;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rView = inflater.inflate(R.layout.fragment_detalle, container, false);
		datos = (DatosSerie) rView.findViewById(R.id.detalles);
		return rView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		imageLoader = new ImageLoader(getActivity());
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (datos != null && m  != null) {
			datos.pTitle.setColor(Color.BLACK);
			datos.pTxt.setColor(Color.BLACK);
			datos.inicializar(m.getTitulo(), m.getSinopsis(), 166, 250);
			imageLoader.DisplayImage(m.getImages(), datos);
		}
	}

}
