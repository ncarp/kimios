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
package org.kimios.kernel.controller;

import java.util.List;

import org.kimios.exceptions.ConfigException;
import org.kimios.kernel.dms.extension.impl.DMEntityAttribute;
import org.kimios.kernel.exception.AccessDeniedException;
import org.kimios.kernel.exception.DataSourceException;
import org.kimios.kernel.security.Session;

public interface IExtensionController
{
    public void setAttribute(Session session, long dmEntityId,
            String attributeName, String attributeValue, boolean indexed)
            throws Exception;

    public DMEntityAttribute getAttribute(Session session, long dmEntityId,
            String attributeName) throws Exception;

    public String getAttributeValue(Session session, long dmEntityId,
            String attributeName) throws Exception;

    public List<DMEntityAttribute> getAttributes(Session session,
            long dmEntityId) throws Exception;

    public String generatePasswordForUser(Session session, String userId, String userSource, boolean sendMail)
            throws ConfigException, DataSourceException, AccessDeniedException;
}
