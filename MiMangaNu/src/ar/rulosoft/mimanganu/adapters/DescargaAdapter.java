package ar.rulosoft.mimanganu.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import ar.rulosoft.mimanganu.ActivityCapitulos;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.services.DescargaCapitulo;

public class DescargaAdapter extends ArrayAdapter<DescargaCapitulo> {

	private LayoutInflater li;
	private static int listItem = R.layout.listitem_descarga;
	ArrayList<DescargaCapitulo> descargas = new ArrayList<DescargaCapitulo>();
	public static String[] estados;

	public DescargaAdapter(Context context, ArrayList<DescargaCapitulo> objects, ActivityCapitulos activityCapitulos) {
		super(context, listItem);
		estados = activityCapitulos.getResources().getStringArray(R.array.estados_descarga);
		li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public DescargaCapitulo getItem(int position) {
		return descargas.get(position);
	}

	@Override
	public int getCount() {
		return descargas.size();
	}

	@Override
	public void add(DescargaCapitulo object) {
		descargas.add(object);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = li.inflate(listItem, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final DescargaCapitulo item = getItem(position);

		if (item != null) {
			String textInfo = " " + estados[item.estado.ordinal()];
			holder.textViewNombre.setText(android.text.Html.fromHtml(item.getCapitulo().getTitulo() + textInfo));
			holder.cargandoProgressBar.setMax(item.getCapitulo().getPaginas());
			holder.cargandoProgressBar.setProgress(item.getProgreso());
			holder.botonImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
				}
			});
		}
		return convertView;
	}

	public void updateAll(ArrayList<DescargaCapitulo> mDescargas) {
		if (mDescargas != null) {
			for (int i = 0; i < mDescargas.size(); i++) {
				boolean esNuevo = true;
				DescargaCapitulo aComparar = mDescargas.get(i);
				for (int j = 0; j < getCount(); j++) {
					if (getItem(j).getCapitulo().getId() == aComparar.getCapitulo().getId()) {
						esNuevo = false;
						DescargaCapitulo item = getItem(j);
						item.setProgreso(aComparar.getProgreso());
						break;
					}
				}
				if (esNuevo) {
					descargas.add(aComparar);
				}
			}
		}
	}

	public static class ViewHolder {
		private TextView textViewNombre;
		private ProgressBar cargandoProgressBar;
		private ImageView botonImageView;

		public ViewHolder(View v) {
			this.textViewNombre = (TextView) v.findViewById(R.id.nombre);
			this.botonImageView = (ImageView) v.findViewById(R.id.boton);
			this.botonImageView.setVisibility(ImageView.INVISIBLE);
			this.cargandoProgressBar = (ProgressBar) v.findViewById(R.id.progreso);
		}
	}

}
