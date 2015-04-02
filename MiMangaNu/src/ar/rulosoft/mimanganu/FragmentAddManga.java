package ar.rulosoft.mimanganu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import ar.rulosoft.mimanganu.adapters.ServerAdapter;
import ar.rulosoft.mimanganu.servers.EsMangaHere;
import ar.rulosoft.mimanganu.servers.EsNineMangaCom;
import ar.rulosoft.mimanganu.servers.HeavenMangaCom;
import ar.rulosoft.mimanganu.servers.ItNineMangaCom;
import ar.rulosoft.mimanganu.servers.KissManga;
import ar.rulosoft.mimanganu.servers.LectureEnLigne;
import ar.rulosoft.mimanganu.servers.MangaFox;
import ar.rulosoft.mimanganu.servers.MangaHere;
import ar.rulosoft.mimanganu.servers.MangaPanda;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.servers.StarkanaCom;
import ar.rulosoft.mimanganu.servers.SubManga;
import ar.rulosoft.mimanganu.servers.TusMangasOnlineCom;

public class FragmentAddManga extends Fragment {

	ListView lista_server;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rView = inflater.inflate(R.layout.fragment_add_manga, container, false);
		lista_server = (ListView) rView.findViewById(R.id.lista_de_servers);
		return rView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		lista_server.setAdapter(new ServerAdapter(getActivity(), new ServerBase[] { new HeavenMangaCom(),
				new SubManga(),// new EsMangaCom(),
				new EsNineMangaCom(), new EsMangaHere(), new TusMangasOnlineCom(), new MangaPanda(), new MangaHere(), new MangaFox(), new StarkanaCom(),
				new KissManga(), new LectureEnLigne(), new ItNineMangaCom() }));
		lista_server.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ServerBase s = (ServerBase) lista_server.getAdapter().getItem(position);
				Intent intent;
				if (s.tieneNavegacionVisual())			
					intent = new Intent(getActivity(), ActivityServerVisualNavegacion.class);// ActivityServerListadeMangas
				else
					intent = new Intent(getActivity(), ActivityServerListadeMangas.class);
				intent.putExtra(ActivityMisMangas.SERVER_ID, s.getServerID());
				getActivity().startActivity(intent);
			}
		});
		super.onActivityCreated(savedInstanceState);
	}

}
