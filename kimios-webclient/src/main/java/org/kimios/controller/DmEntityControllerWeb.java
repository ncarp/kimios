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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kimios.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kimios.client.controller.DocumentController;
import org.kimios.client.controller.FolderController;
import org.kimios.client.controller.SearchController;
import org.kimios.client.controller.WorkspaceController;
import org.kimios.client.controller.helpers.XMLGenerators;
import org.kimios.core.wrappers.DMEntity;
import org.kimios.kernel.ws.pojo.Document;
import org.kimios.kernel.ws.pojo.Folder;
import org.kimios.kernel.ws.pojo.Meta;
import org.kimios.kernel.ws.pojo.Workspace;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * @author Fabien Alin
 */
public class DmEntityControllerWeb extends Controller
{
    public DmEntityControllerWeb(Map<String, String> parameters)
    {
        super(parameters);
    }

    public String execute() throws Exception
    {
        if (action.equalsIgnoreCase("getEntities")) {
            return getEntities();
        }
        if (action.equalsIgnoreCase("getEntity")) {
            return getEntity();
        }
        if (action.equalsIgnoreCase("deleteEntity")) {
            deleteEntity();
        }
        if (action.equalsIgnoreCase("getPath")) {
            return getPath();
        }
        if (action.equalsIgnoreCase("moveEntity")) {
            moveEntity();
        }
        if (action.equalsIgnoreCase("moveEntities")) {
            moveEntities();
        }
        if (action.equalsIgnoreCase("deleteEntities")) {
            deleteEntities();
        }
        if (action.equalsIgnoreCase("updateEntities")) {
            updateEntities();
        }
        return "";
    }

    private String getEntity() throws Exception
    {
        int dmEntityType = Integer.parseInt(parameters.get("dmEntityType"));
        long dmEntityUid = Long.parseLong(parameters.get("dmEntityUid"));
        org.kimios.core.wrappers.DMEntity it = null;
        switch (dmEntityType) {
            case 1:
                it = new DMEntity(workspaceController.getWorkspace(sessionUid, dmEntityUid));
                break;
            case 2:
                it = new DMEntity(folderController.getFolder(sessionUid, dmEntityUid));
                break;
            case 3:
                it = new DMEntity(documentController.getDocument(sessionUid, dmEntityUid));
                break;
        }
        if (it != null) {
            String jsonResp = "";
            jsonResp = new JSONSerializer().serialize(it);
            return "[" + jsonResp + "]";
        } else {
            throw new Exception();
        }
    }

    private void deleteEntity() throws Exception
    {
        int dmEntityType = Integer.parseInt(parameters.get("dmEntityType"));
        long dmEntityUid = Long.parseLong(parameters.get("dmEntityUid"));
        switch (dmEntityType) {
            case 1:
                workspaceController.deleteWorkspace(sessionUid, dmEntityUid);
                break;
            case 2:
                folderController.deleteFolder(sessionUid, dmEntityUid);
                break;
            case 3:
                documentController.deleteDocument(sessionUid, dmEntityUid);
                break;
        }
    }

    private String getEntities() throws Exception
    {
        int dmEntityType = 0;
        long dmEntityUid = 0;
        List<DMEntity> qNodes = new ArrayList<DMEntity>();
        try {
            try {
                dmEntityType = Integer.parseInt(parameters.get("dmEntityType"));
                dmEntityUid = Long.parseLong(parameters.get("dmEntityUid"));
            } catch (Exception e) {

            }

            if (dmEntityType > 0 && dmEntityUid > 0) {
                Folder[] fNodes = folderController.getFolders(sessionUid, dmEntityUid, dmEntityType);
                for (Folder f : fNodes) {
                    qNodes.add(new DMEntity(f));
                }
                if (dmEntityType == 2) {
                    Document[] dNodes = documentController.getDocuments(sessionUid, dmEntityUid);
                    for (Document d : dNodes) {
                        try {
                            qNodes.add(new DMEntity(d));
                        } catch (Exception e) {

                        }
                    }
                }
            } else {
                Workspace[] wNodes = workspaceController.getWorkspaces(sessionUid);
                for (Workspace w : wNodes) {
                    qNodes.add(new DMEntity(w));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String jsonResp = new JSONSerializer().exclude("class").serialize(qNodes);
        jsonResp = "{list:" + jsonResp + "}";

        return jsonResp;
    }

    private String getPath() throws Exception
    {
        try {
            int dmEntityType = Integer.parseInt(parameters.get("dmEntityType"));
            long dmEntityUid = Long.parseLong(parameters.get("dmEntityUid"));
            String path = new SearchController().getPathFromDMEntity(sessionUid, dmEntityUid, dmEntityType);
            return "[{'dmEntityUid':" + dmEntityUid + ",'dmEntityType':" + dmEntityType + ",'path':'" + path + "'}]";
        } catch (NumberFormatException nfe) {
            return "[{'dmEntityUid':0,'dmEntityType':0,'path':'/'}]";
        }
    }

    private void moveEntity() throws Exception
    {
        long dmEntityUid = Long.parseLong(parameters.get("uid"));
        int dmType = Integer.parseInt(parameters.get("type"));
        long targetUid = Long.parseLong(parameters.get("targetUid"));
        int targetType = Integer.parseInt(parameters.get("targetType"));
        if (dmType == 2) {
            Folder f = folderController.getFolder(sessionUid, dmEntityUid);
            if (f != null) {
                if (!(f.getUid() == targetUid && targetType == 2)) {
                    f.setParentUid(targetUid);
                    f.setParentType(targetType);
                    folderController.updateFolder(sessionUid, f);
                }
            }
        }
        if (dmType == 3) {
            Document doc = documentController.getDocument(sessionUid, dmEntityUid);
            if (doc != null && targetType == 2) {
                doc.setFolderUid(targetUid);
                documentController.updateDocument(sessionUid, doc);
            }
        }
    }

    private void moveEntities() throws Exception
    {
        long targetUid = Long.parseLong(parameters.get("targetUid"));
        int targetType = Integer.parseInt(parameters.get("targetType"));

        List<Map<String, Object>> dmEntities = (ArrayList<Map<String, Object>>) new JSONDeserializer()
                .deserialize(parameters.get("dmEntityPojosJson"));

        for (Map<String, Object> dmEntity : dmEntities) {
            long dmEntityUid = Long.parseLong(String.valueOf(dmEntity.get("uid")));
            int dmType = Integer.parseInt(String.valueOf(dmEntity.get("type")));

            if (dmType == 2) {
                Folder f = folderController.getFolder(sessionUid, dmEntityUid);
                if (f != null) {
                    if (!(f.getUid() == targetUid && targetType == 2)) {
                        f.setParentUid(targetUid);
                        f.setParentType(targetType);
                        folderController.updateFolder(sessionUid, f);
                    }
                }
            }
            if (dmType == 3) {
                Document doc = documentController.getDocument(sessionUid, dmEntityUid);
                if (doc != null && targetType == 2) {
                    doc.setFolderUid(targetUid);
                    documentController.updateDocument(sessionUid, doc);
                }
            }
        }
    }

    private void deleteEntities() throws Exception
    {
        List<Map<String, Object>> dmEntities = (ArrayList<Map<String, Object>>) new JSONDeserializer()
                .deserialize(parameters.get("dmEntityPojosJson"));
        for (Map<String, Object> dmEntity : dmEntities) {
            long dmEntityUid = Long.parseLong(String.valueOf(dmEntity.get("uid")));
            int dmEntityType = Integer.parseInt(String.valueOf(dmEntity.get("type")));

            switch (dmEntityType) {
                case 1:
                    workspaceController.deleteWorkspace(sessionUid, dmEntityUid);
                    break;
                case 2:
                    folderController.deleteFolder(sessionUid, dmEntityUid);
                    break;
                case 3:
                    documentController.deleteDocument(sessionUid, dmEntityUid);
                    break;
            }
        }
    }

    private void updateEntities() throws Exception
    {
        List<Map<String, Object>> dmEntities = (ArrayList<Map<String, Object>>) new JSONDeserializer()
                .deserialize(parameters.get("dmEntityPojosJson"));

        for (Map<String, Object> dmEntity : dmEntities) {
            long dmEntityUid = Long.parseLong(String.valueOf(dmEntity.get("uid")));
            int dmType = Integer.parseInt(String.valueOf(dmEntity.get("type")));
            String name = String.valueOf(dmEntity.get("name"));

            switch (dmType) {
                case 1:
                    WorkspaceController wctr = workspaceController;
                    Workspace w = wctr.getWorkspace(sessionUid, dmEntityUid);
                    w.setName(name);
                    wctr.updateWorkspace(sessionUid, w);
                    if (securityController.hasFullAccess(sessionUid, dmEntityUid, 1)) {
                        boolean changeSecurity = true;
                        if (parameters.get("changeSecurity") != null) {
                            changeSecurity = Boolean.parseBoolean(parameters.get("changeSecurity"));
                        }
                        if (changeSecurity == true) {
                            securityController.updateDMEntitySecurities(sessionUid, dmEntityUid, 1,
                                    (parameters.get("isRecursive") != null && parameters.get(
                                            "isRecursive").equals("true")),
                                    DMEntitySecuritiesParser.parseFromJson(parameters.get("sec"), dmEntityUid, 1));
                        }
                    }
                    break;
                case 2:
                    FolderController fctr = folderController;
                    Folder f = fctr.getFolder(sessionUid, dmEntityUid);
                    f.setName(name);
                    fctr.updateFolder(sessionUid, f);
                    boolean recursive = false;
                    recursive = parameters.get("isRecursive") != null && parameters.get("isRecursive").equals("true");
                    if (securityController.hasFullAccess(sessionUid, f.getUid(), 2)) {
                        boolean changeSecurity = true;
                        if (parameters.get("changeSecurity") != null) {
                            changeSecurity = Boolean.parseBoolean(parameters.get("changeSecurity"));
                        }
                        if (changeSecurity == true) {
                            securityController.updateDMEntitySecurities(sessionUid, dmEntityUid, 2, recursive,
                                    DMEntitySecuritiesParser.parseFromJson(
                                            parameters.get("sec"), dmEntityUid, 2));
                        }
                    }
                    break;
                case 3:
                    DocumentController dsm = documentController;
                    Document d = dsm.getDocument(sessionUid, dmEntityUid);
                    if (!d.getName().equals(name)) {
                        d.setName(name);
                        dsm.updateDocument(sessionUid, d);
                    }
                    long docType = Long.parseLong(parameters.get("documentTypeUid"));

                    String sec = parameters.get("sec");
                    String metaValues = parameters.get("metaValues");

                    if (parameters.get("changeSecurity") != null &&
                            Boolean.parseBoolean(parameters.get("changeSecurity")) == true)
                    {
                        securityController.updateDMEntitySecurities(sessionUid, dmEntityUid, 3, false,
                                DMEntitySecuritiesParser.parseFromJson(sec, dmEntityUid, 3));
                    }
                    Map<Meta, String> mMetasValues = DMEntitySecuritiesParser
                            .parseMetasValuesFromJson(sessionUid, metaValues, documentVersionController);
                    String xmlMeta = XMLGenerators.getMetaDatasDocumentXMLDescriptor(mMetasValues, "yyyy-MM-dd");
                    documentVersionController.updateDocumentVersion(sessionUid, d.getUid(), docType, xmlMeta);
                    break;
            }
        }
    }
}

