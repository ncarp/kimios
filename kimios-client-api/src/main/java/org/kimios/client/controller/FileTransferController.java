/*
 * Kimios - Document Management System Software
 * Copyright (C) 2012-2013  DevLib'
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kimios.client.controller;

import org.kimios.client.controller.helpers.ByteArrayDataSource;
import org.kimios.client.controller.helpers.FileCompressionHelper;
import org.kimios.client.controller.helpers.HashCalculator;
import org.kimios.client.exception.AccessDeniedException;
import org.kimios.client.exception.ConfigException;
import org.kimios.client.exception.DMSException;
import org.kimios.client.exception.ExceptionHelper;
import org.kimios.client.exception.TransferIntegrityException;
import org.kimios.kernel.ws.pojo.DataTransaction;
import org.kimios.webservices.DocumentVersionService;
import org.kimios.webservices.FileTransferService;

import java.io.*;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.zip.ZipInputStream;

/**
 * FileTransferController contains some methods that are used to manage the file
 * transfers between DMS Server and clients
 */
public class FileTransferController {

    private String temporaryFilesPath;
    private FileTransferService client;
    private int chunkSize = 0;

    public DocumentVersionService documentVersionClient;

    public DocumentVersionService getDocumentVersionClient() {
        return documentVersionClient;
    }

    public void setDocumentVersionClient(DocumentVersionService documentVersionClient) {
        this.documentVersionClient = documentVersionClient;
    }

    public FileTransferService getClient() {
        return client;
    }

    public void setClient(FileTransferService client) {
        this.client = client;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public long uploadFileFirstVersion(String sessionId, long documentId,
                                       InputStream fin, boolean isCompressed) throws Exception,
            TransferIntegrityException
    {
        try {

            long documentVersionId = documentVersionClient.createDocumentVersion(sessionId, documentId);
            InputStream in = null;
            if (isCompressed) {
                File toUpload = FileCompressionHelper.getUploadableCompressedVersion(
                        fin, sessionId, temporaryFilesPath);
                in = new FileInputStream(toUpload);
            } else {
                in = fin;
            }
            DataTransaction transaction = client.startUploadTransaction(
                    sessionId,
                    documentId,
                    isCompressed
            );
            doSendChunk(sessionId, transaction, in, chunkSize);
            return documentVersionId;
        } catch (Exception e) {
            throw new ExceptionHelper().convertException(e);
        }
    }

    /**
     * Write data to update temporary file of a given upload transaction
     */
    private void doSendChunk(String sessionId, DataTransaction dt,
                             InputStream in, int chunkSize) throws Exception {
        String hashMD5 = "";
        String hashSHA = "";
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        ByteArrayDataSource bads = new ByteArrayDataSource();
        byte[] data = null;
        int bufferSize = chunkSize;
        data = new byte[bufferSize];
        try {
            int nbBytes = 0;
            while ((nbBytes = in.read(data, 0, data.length)) != -1) {
                while (nbBytes < chunkSize) {
                    int nbBis = in.read(data, nbBytes, chunkSize - nbBytes);
                    if (nbBis == -1) {
                        break;
                    }
                    nbBytes += nbBis;
                }
                if (nbBytes < data.length) {
                    client.sendChunk(sessionId, dt.getUid(), Arrays.copyOfRange(data, 0, nbBytes));
                } else
                    client.sendChunk(sessionId, dt.getUid(), data);
                md5.update(data, 0, nbBytes);
                sha1.update(data, 0, nbBytes);
            }
        } catch (Exception ex) {
            throw ex;
        }
        hashMD5 = HashCalculator.buildHexaString(md5.digest()).replaceAll(" ", "");
        hashSHA = HashCalculator.buildHexaString(sha1.digest()).replaceAll(" ", "");
        client.endUploadTransaction(sessionId, dt.getUid(), hashMD5, hashSHA);
    }

    public long uploadFileNewVersion(String sessionId, long documentId,
                                     InputStream fin, boolean isCompressed) throws Exception,
            DMSException, ConfigException, AccessDeniedException,
            TransferIntegrityException {
        try {

            long docVersionId = documentVersionClient.createDocumentVersionFromLatest(sessionId, documentId);
            InputStream in = null;
            if (isCompressed) {
                File toUpload = FileCompressionHelper.getUploadableCompressedVersion(
                        fin, sessionId, temporaryFilesPath);
                in = new FileInputStream(toUpload);
            } else {
                in = fin;
            }
            DataTransaction transaction = client.startUploadTransaction(sessionId, documentId, isCompressed);
            doSendChunk(sessionId, transaction, in, chunkSize);
            return docVersionId;
        } catch (Exception e) {
            throw new ExceptionHelper().convertException(e);
        }
    }

    public void uploadFileUpdateVersion(String sessionId, long documentId,
                                        InputStream fin, boolean isCompressed) throws Exception,
            DMSException, ConfigException, AccessDeniedException,
            TransferIntegrityException {
        try {

            InputStream in = null;
            if (isCompressed) {
                File toUpload = FileCompressionHelper.getUploadableCompressedVersion(
                        fin, sessionId, temporaryFilesPath);
                in = new FileInputStream(toUpload);
            } else {
                in = fin;
            }
            DataTransaction transaction = client.startUploadTransaction(sessionId, documentId, isCompressed);
            doSendChunk(sessionId, transaction, in, chunkSize);
        } catch (Exception e) {
            throw new ExceptionHelper().convertException(e);
        }
    }

    public void downloadTemporaryFile(String sessionId,
                                      String temporaryFilePath, OutputStream out, long length) {
        try {
            FileInputStream in = new FileInputStream(new File(temporaryFilePath));
            byte[] b = new byte[chunkSize];
            while (true) {
                int c = in.read(b);
                if (c == -1) {
                    break;
                }
                out.write(b);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadFileVersion(String sessionId, long documentVersionId,
                                    OutputStream os, boolean isCompressed) throws Exception,
            DMSException, ConfigException, AccessDeniedException {
        try {
            DataTransaction transac = client.startDownloadTransaction(sessionId, documentVersionId, isCompressed);

            int bufferSize = chunkSize;
            String tmpFileName = "";
            OutputStream tmp;
            if (isCompressed) {
                tmpFileName = FileCompressionHelper.getTempFilePath(temporaryFilesPath,
                        "dl" + sessionId);
                tmp = new BufferedOutputStream(new FileOutputStream(temporaryFilesPath
                        + tmpFileName));
            } else {
                tmp = os;
            }

            // Read Stream from server...
            long offset = 0;
            InputStream in;
            int readBytes;
            // Read packets ...
            while (offset < transac.getSize()) {
                if (offset + bufferSize > transac.getSize()) {
                    bufferSize = new Long(transac.getSize() - offset).intValue();
                }
                byte[] t =client.getChunck(sessionId, transac.getUid(), offset, bufferSize);
                try {
                    tmp.write(t, 0, t.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                offset += bufferSize;
            }

            tmp.flush();
            tmp.close();

            if (isCompressed) {
                InputStream inFull = new FileInputStream(temporaryFilesPath
                        + tmpFileName);
                InputStream toSend = null;
                // Uncompress
                toSend = new ZipInputStream(inFull);
                if (((ZipInputStream) toSend).getNextEntry() == null) {
                    throw new Exception("Zip error");
                }
                int d = -1;
                while ((d = toSend.read()) != -1 && toSend.available() > 0) {
                    os.write(d);
                }

                os.flush();
                os.close();
            }
        } catch (Exception e) {
            throw new ExceptionHelper().convertException(e);
        }
    }

    /**
     * Get temporary files path
     */
    public String getTemporaryFilesPath() {
        return temporaryFilesPath;
    }

    /**
     * Set temporary files path
     */
    public void setTemporaryFilesPath(String temporaryFilesPath) {
        this.temporaryFilesPath = temporaryFilesPath;
    }
}
