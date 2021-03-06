/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.tigers.sumatra.data.collector.IExportable;


/**
 * With {@link CSVExporter} you can export user-defined values to csv-files on disc
 * Usage:
 * create an instance
 * optionally set a header
 * add values as often as applicable
 * close the instance
 *
 * <pre>
 * CSVExporter.createInstance(&quot;test&quot;, &quot;testexport&quot;, true);
 * CSVExporter exporter = CSVExporter.getInstance(&quot;test&quot;);
 *
 * exporter.setHeader(&quot;first&quot;, &quot;second&quot;, &quot;third&quot;);
 *
 * exporter.addValues(&quot;1&quot;, &quot;3&quot;, &quot;hallo&quot;);
 * exporter.addValues(&quot;3&quot;, &quot;44&quot;, &quot;goodbye&quot;);
 *
 * exporter.close();
 * </pre>
 *
 * calls to createInstance and addValues can be distributed to different classes
 * for example an instance is created in a role, but values are added from a skill
 */
public final class CSVExporter
{
	private static final Logger log = LogManager.getLogger(CSVExporter.class.getName());
	private boolean autoIncrement;
	private final String fileName;
	private final Queue<String> values = new LinkedList<>();
	private final Queue<String> header = new LinkedList<>();
	private String additionalInfo = "";
	private File file;
	private FileOutputStream fileOutputStream;
	private BufferedWriter fileWriter;
	private boolean writeHeader = false;
	private static final String DELIMITER = ",";
	private int headerSize = 0;

	private boolean isClosed = false;
	private boolean append;


	/**
	 * @param fileName
	 * @param autoIncrement
	 * @param append
	 */
	public CSVExporter(final String fileName, final boolean autoIncrement, final boolean append)
	{
		this.fileName = fileName;
		this.autoIncrement = autoIncrement;
		this.append = append;
	}


	/**
	 * @param fileName subtract-dir and name of exported file without .csv ending
	 * @param autoIncrement
	 */
	public CSVExporter(final String fileName, final boolean autoIncrement)
	{
		this(fileName, autoIncrement, false);
	}


	/**
	 * @param folder
	 * @param key
	 * @param list
	 */
	public static void exportCollection(final String folder, final String key,
			final Collection<? extends IExportable> list)
	{
		CSVExporter exporter = new CSVExporter(folder + "/" + key, false);
		if (!list.isEmpty())
		{
			IExportable firstElement = list.iterator().next();
			Collection<String> header = firstElement.getHeaders();
			if (header.size() == firstElement.getNumberList().size())
			{
				exporter.setHeader(header);
			} else if (!header.isEmpty())
			{
				log.warn("Element size does not match header size: {} != {}", header.size(),
						firstElement.getNumberList().size());
			}
			list.forEach(nl -> exporter.addValues(nl.getNumberList()));
		} else
		{
			exporter.persistRecord();
		}
		exporter.close();
	}


	/**
	 * adds a new data set to a file.
	 *
	 * @param values list of values. note: count of values has to match the header
	 */
	public void addValues(final Number... values)
	{
		this.values.clear();
		for (final Number f : values)
		{
			this.values.add(String.valueOf(f));
		}
		persistRecord();
	}


	/**
	 * adds a new data set to a file.
	 *
	 * @param values list of values. note: count of values has to match the header
	 */
	public void addValues(final Collection<? extends Number> values)
	{
		this.values.clear();
		for (final Object f : values)
		{
			this.values.add(String.valueOf(f));
		}
		persistRecord();
	}


	/**
	 * This method writes all content the fields of the given object into the file
	 *
	 * @param bean
	 * @throws IllegalAccessException If one of the fields of the given bean is not accessible
	 */
	public void addValuesBean(final Object bean) throws IllegalAccessException
	{
		values.clear();

		Field[] fields = bean.getClass().getDeclaredFields();
		for (Field field : fields)
		{
			values.add(String.valueOf(field.get(bean)));
		}
		persistRecord();
	}


	/**
	 * set the csv header (first row of the file)
	 * as soon a header is set, it will be written to the file
	 *
	 * @param header note: value count has to match header count
	 */
	public void setHeader(final String... header)
	{
		this.header.clear();
		this.header.addAll(Arrays.asList(header));
		writeHeader = true;
		headerSize = this.header.size();
	}


	/**
	 * set the csv header
	 *
	 * @param header
	 */
	public void setHeader(Collection<String> header)
	{
		this.header.clear();
		this.header.addAll(header);
		writeHeader = true;
		headerSize = this.header.size();
	}


	/**
	 * This method uses reflection to get the available field names and fills a header with them
	 *
	 * @param bean
	 */
	public void setHeaderBean(final Object bean)
	{
		header.clear();

		Field[] fields = bean.getClass().getDeclaredFields();
		for (Field field : fields)
		{
			header.add(field.getName());
		}

		writeHeader = true;
		headerSize = header.size();
	}


	/**
	 * The additional info will be printed above the header.
	 * Header must be set
	 *
	 * @param info
	 */
	public void setAdditionalInfo(final String info)
	{
		additionalInfo = info;
	}


	/**
	 * do the actual persisting stuff
	 */
	private void persistRecord()
	{
		try
		{
			if (file == null)
			{
				File dir = new File(fileName).getParentFile();
				if (!dir.exists())
				{
					boolean created = dir.mkdirs();
					if (!created)
					{
						log.warn("Could not create export dir: {}", dir.getAbsolutePath());
					}
				}
				int counter = 0;
				if (autoIncrement)
				{
					while ((file = new File(fileName + counter + ".csv")).exists())
					{
						counter++;
					}
				} else
				{
					file = new File(fileName + ".csv");
				}

				fileOutputStream = new FileOutputStream(file, append);
				fileWriter = new BufferedWriter(new OutputStreamWriter(
						fileOutputStream, StandardCharsets.UTF_8));

				if (writeHeader)
				{
					fileWriter.write("#Sumatra CSVExporter\n");
					fileWriter.write("#" + new Date().toString() + "\n");

					if (!additionalInfo.isEmpty())
					{
						fileWriter.write("#" + additionalInfo + "\n");
					}

					fileWriter.write(header.poll());
					for (final String s : header)
					{
						fileWriter.write(DELIMITER + s);
					}
					fileWriter.write("\n");
					fileWriter.flush();
				}

			}
			if (writeHeader && (headerSize != values.size()))
			{
				throw new CSVExporterException("object count on values must match header", null);
			}
			if (!values.isEmpty())
			{
				fileWriter.write(values.poll());
				for (final String s : values)
				{
					fileWriter.write(DELIMITER + s);
				}
				fileWriter.write("\n");
				fileWriter.flush();
			}
		} catch (final FileNotFoundException err)
		{
			throw new CSVExporterException("file not found", err);
		} catch (final IOException err)
		{
			throw new CSVExporterException("io error", err);
		}

	}


	/**
	 * @return
	 */
	public String getAbsoluteFileName()
	{
		return new File(fileName + ".csv").getAbsolutePath();
	}


	/**
	 * closes the file stream.
	 * do not forget to call this method when you are done
	 */
	public void close()
	{
		if (fileWriter != null)
		{
			try
			{
				fileWriter.close();
			} catch (final IOException err)
			{
				throw new CSVExporterException("io error while closing the file", err);
			}
		}
		if (fileOutputStream != null)
		{
			try
			{
				fileOutputStream.close();
			} catch (final IOException err)
			{
				throw new CSVExporterException("io error while closing the file", err);
			}
		}
		log.debug("Saved csv file to {}", fileName);
		isClosed = true;
	}


	/**
	 * @return
	 */
	public boolean isClosed()
	{
		return isClosed;
	}


	/**
	 * @return
	 */
	public boolean isEmpty()
	{
		return values.isEmpty();
	}
}
