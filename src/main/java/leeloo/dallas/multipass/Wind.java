package leeloo.dallas.multipass;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leeloo.dallas.multipass.model.Acoustid;
import leeloo.dallas.multipass.model.Recording;
import leeloo.dallas.multipass.model.Releasegroup;
import leeloo.dallas.multipass.model.Result;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.id3.framebody.AbstractFrameBodyTextInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Wind implements Element {
	Logger log = LoggerFactory.getLogger(this.getClass());
	private static Wind instance = null;
	private static String FILE = "FILE";
	private static String FINGERPRINT = "fingerprint";
	private static String DURATION = "duration";
	private static String CLIENT = "client";
	private static String CLIENT_KEY = "WKASbrzJ";
	private static String META = "meta";
	private static String META_DATA_PARAMS = "recordings recordingids releases releaseids releasegroups releasegroupids tracks compress usermeta sources";

	protected Wind() {
		// Exists only to defeat instantiation.
	}

	public static Element getStone() {
		if (instance == null) {
			instance = new Wind();
		}
		return instance;
	}

	@Override
	public void process(File file) {
		URL CMD = getClass().getResource("/fpcalc.exe");
		String[] command = { CMD.getPath(), file.getAbsolutePath() };
		ProcessBuilder probuilder = new ProcessBuilder(command);
		try {
			Process process = probuilder.start();

			// Read out dir output
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			Map<String, String> fpMap = new HashMap<>();
			while ((line = br.readLine()) != null) {
				log.info(line);
				String[] rawInfo = line.split("=");
				fpMap.put(rawInfo[0].toLowerCase(), rawInfo[1]);
			}

			int exitValue = process.waitFor();
			log.debug("\n\nExit Value is " + exitValue);

			String request = "http://api.acoustid.org/v2/lookup";

			URI uri = new URIBuilder().setScheme("http")
					.setHost("api.acoustid.org").setPath("/v2/lookup")
					.setParameter(CLIENT, CLIENT_KEY)
					.setParameter(META, META_DATA_PARAMS)
					.setParameter(DURATION, fpMap.get(DURATION))
					.setParameter(FINGERPRINT, fpMap.get(FINGERPRINT)).build();
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(uri);
			HttpResponse response = httpclient.execute(httppost);
			Acoustid acoustid = handleResponse(response);
			
			List<Result> results = acoustid.getResults();
			Collections.sort(results, new Comparator<Result>() {

				@Override
				public int compare(Result o1, Result o2) {
					double score1 = o1.getScore();
					double score2 = o2.getScore();
					if(score1>score2)
						return 1;
					else if (score1==score2)
					return 0;
					else
						return -1;
				}
			});
			Result result = results.get(0);
			List<Recording> recordings = result.getRecordings();
			Recording recording =recordings.get(0);
			List<Releasegroup> releasegroups = recording.getReleasegroups(); 
			Releasegroup releasegroup= releasegroups.get(0);
			
			
			String fileName = file.getName();
			int i = fileName.lastIndexOf('.');
			String extension = fileName.substring(i + 1);
			MP3File f = null;

			f = (MP3File) AudioFileIO.read(file);

			f.hasID3v1Tag();
			Tag tag = f.getTag();
			// ID3v1Tag v1Tag = (ID3v1Tag)tag;
			AbstractID3v2Tag v2tag = f.getID3v2Tag();

			AbstractID3v2Frame frame = v2tag
					.getFirstField(ID3v24Frames.FRAME_ID_ARTIST);

			if (frame.getBody() instanceof AbstractFrameBodyTextInfo) {
				AbstractFrameBodyTextInfo textBody = (AbstractFrameBodyTextInfo) frame
						.getBody();
				System.out.println("TagArtist: " +textBody + " AcoustidArtist: "+ recording.getArtists().get(0).getName());
			}
			 frame = v2tag
					.getFirstField(ID3v24Frames.FRAME_ID_ALBUM);

			if (frame.getBody() instanceof AbstractFrameBodyTextInfo) {
				AbstractFrameBodyTextInfo textBody = (AbstractFrameBodyTextInfo) frame
						.getBody();
				System.out.println("TagAlbum: " +textBody + " AcoustidAlbum: "+ releasegroup.getTitle());
			}
			
			 frame = v2tag
						.getFirstField(ID3v24Frames.FRAME_ID_TITLE);
				if (frame.getBody() instanceof AbstractFrameBodyTextInfo) {
					AbstractFrameBodyTextInfo textBody = (AbstractFrameBodyTextInfo) frame
							.getBody();
					System.out.println("TagTitle: " +textBody + " AcoustidTitle: "+ recording.getTitle());
				}
		} catch (CannotReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TagException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReadOnlyFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAudioFrameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// response.close();
		}

	}

	public Acoustid handleResponse(final HttpResponse response)
			throws IOException {
		StatusLine statusLine = response.getStatusLine();
		System.out.println(statusLine);
		HttpEntity entity = response.getEntity();
		if (statusLine.getStatusCode() >= 300) {
			throw new HttpResponseException(statusLine.getStatusCode(),
					statusLine.getReasonPhrase());
		}
		if (entity == null) {
			throw new ClientProtocolException("Response contains no content");
		}
		Gson gson = new GsonBuilder().create();
		ContentType contentType = ContentType.getOrDefault(entity);
		Charset charset = contentType.getCharset();
		InputStreamReader reader = new InputStreamReader(entity.getContent(),
				charset);
		StringWriter writer = new StringWriter();
		IOUtils.copy(reader, writer);
		String theString = writer.toString();
		System.out.println(theString);
		// while(linetest!=null)
		// {linetest = br.readLine();System.out.println(linetest);}
		return gson.fromJson(theString, Acoustid.class);
	}

}
