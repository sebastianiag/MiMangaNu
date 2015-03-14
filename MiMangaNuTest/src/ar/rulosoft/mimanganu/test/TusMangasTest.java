package ar.rulosoft.mimanganu.test;

import org.junit.Before;

import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.EsMangaCom;

public class TusMangasTest extends TestBase {

	@Before
	public void setUp() throws Exception {
		server = new EsMangaCom();
		manga = new Manga(server.getServerID(), "bhbk", "http://www.tumangaonline.com/listado-mangas/manga/8865/B+Gata+H+Kei", false);
		capitulo = new Capitulo("uno", "http://www.tumangaonline.com/index.php?option=com_controlmanga&view=capitulos&format=raw&idManga=8865&idCapitulo=300");
	}
	
}
