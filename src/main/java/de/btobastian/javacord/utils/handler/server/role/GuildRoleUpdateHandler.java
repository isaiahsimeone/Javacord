package de.btobastian.javacord.utils.handler.server.role;

import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.entities.permissions.Permissions;
import de.btobastian.javacord.entities.permissions.impl.ImplPermissions;
import de.btobastian.javacord.entities.permissions.impl.ImplRole;
import de.btobastian.javacord.events.server.role.RoleChangePermissionsEvent;
import de.btobastian.javacord.events.server.role.RoleChangePositionEvent;
import de.btobastian.javacord.listeners.server.role.RoleChangePermissionsListener;
import de.btobastian.javacord.listeners.server.role.RoleChangePositionListener;
import de.btobastian.javacord.utils.PacketHandler;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the guild role create packet.
 */
public class GuildRoleUpdateHandler extends PacketHandler {

    /**
     * Creates a new instance of this class.
     *
     * @param api The api.
     */
    public GuildRoleUpdateHandler(DiscordApi api) {
        super(api, true, "GUILD_ROLE_UPDATE");
    }

    @Override
    public void handle(JSONObject packet) {
        JSONObject roleJson = packet.getJSONObject("role");
        long roleId = Long.parseLong(roleJson.getString("id"));
        api.getRoleById(roleId).map(role -> (ImplRole) role).ifPresent(role -> {
            Permissions oldPermissions = role.getPermissions();
            ImplPermissions newPermissions = new ImplPermissions(roleJson.getInt("permissions"), 0);
            if (!oldPermissions.equals(newPermissions)) {
                role.setPermissions(newPermissions);

                RoleChangePermissionsEvent event =
                        new RoleChangePermissionsEvent(api, role, newPermissions, oldPermissions);

                List<RoleChangePermissionsListener> listeners = new ArrayList<>();
                listeners.addAll(role.getRoleChangePermissionsListeners());
                listeners.addAll(role.getServer().getRoleChangePermissionsListeners());
                listeners.addAll(api.getRoleChangePermissionsListeners());

                dispatchEvent(listeners, listener -> listener.onRoleChangePermissions(event));
            }

            int oldPosition = role.getPosition();
            int newPosition = roleJson.getInt("position");
            if (oldPosition != newPosition) {
                role.setPosition(newPosition);

                RoleChangePositionEvent event = new RoleChangePositionEvent(api, role, newPosition, oldPosition);

                List<RoleChangePositionListener> listeners = new ArrayList<>();
                listeners.addAll(role.getRoleChangePositionListeners());
                listeners.addAll(role.getServer().getRoleChangePositionListeners());
                listeners.addAll(api.getRoleChangePositionListeners());

                dispatchEvent(listeners, listener -> listener.onRoleChangePosition(event));
            }
        });
    }

}