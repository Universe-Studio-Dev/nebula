package github.universe.studio.nebula.velocity.others;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author DanielH131COL
 * @created 05/09/2025
 * @project nebula
 * @file FriendManager
 */
public class FriendManager {
    private final Map<UUID, Set<UUID>> friendLists = new HashMap<>();
    private final Map<UUID, Set<UUID>> pendingRequests = new HashMap<>();
    private final ConfigManager configManager;
    private final ProxyServer server;

    private static final String CANNOT_ADD_SELF = "<red>You cannot add yourself as a friend!";
    private static final String ALREADY_FRIEND = "<red>%player% is already your friend!";
    private static final String REQUEST_ALREADY_SENT = "<red>You already sent a friend request to %player%!";
    private static final String REQUEST_SENT = "<green>Friend request sent to %player%!";
    private static final String REQUEST_RECEIVED = "<green>%player% sent you a friend request! Use /friend accept %sender% or /friend deny %sender%";
    private static final String NO_REQUEST = "<red>No friend request from %player%!";
    private static final String REQUEST_ACCEPTED = "<green>You accepted %player%'s friend request!";
    private static final String REQUEST_DENIED = "<green>You denied %player%'s friend request!";
    private static final String REQUEST_DENIED_BY = "<red>%player% denied your friend request!";
    private static final String ADDED = "<green>%player% is now your friend!";
    private static final String NOT_FRIEND = "<red>%player% is not your friend!";
    private static final String REMOVED = "<green>Removed %player% from your friends!";
    private static final String REMOVED_BY = "<red>%player% removed you from their friends!";
    private static final String LIST_EMPTY = "<red>You have no friends!";
    private static final String LIST_HEADER = "<green>Your Friends:";
    private static final String LIST_ENTRY = "<green>- %player%: %status%";
    private static final String OFFLINE = "<red>%player% is offline!";
    private static final String JOINED = "<green>Joined %player%'s server!";
    private static final String CONNECTED = "<green>%player% connected to %server%!";
    private static final String DISCONNECTED = "<red>%player% disconnected!";

    public FriendManager(ConfigManager configManager, ProxyServer server) {
        this.configManager = configManager;
        this.server = server;
    }

    public boolean sendFriendRequest(Player sender, Player target) throws Exception {
        UUID senderId = sender.getUniqueId();
        UUID targetId = target.getUniqueId();
        if (senderId.equals(targetId)) {
            sender.sendMessage(CC.translateToComponent(CANNOT_ADD_SELF));
            return false;
        }
        Set<UUID> friends = friendLists.computeIfAbsent(senderId, k -> new HashSet<>());
        if (friends.contains(targetId)) {
            sender.sendMessage(CC.translateToComponent(ALREADY_FRIEND.replace("%player%", target.getUsername())));
            return false;
        }
        Set<UUID> requests = pendingRequests.computeIfAbsent(targetId, k -> new HashSet<>());
        if (requests.contains(senderId)) {
            sender.sendMessage(CC.translateToComponent(REQUEST_ALREADY_SENT.replace("%player%", target.getUsername())));
            return false;
        }
        requests.add(senderId);
        savePendingRequests(targetId);
        sender.sendMessage(CC.translateToComponent(REQUEST_SENT.replace("%player%", target.getUsername())));
        target.sendMessage(CC.translateToComponent(REQUEST_RECEIVED.replace("%player%", sender.getUsername()).replace("%sender%", sender.getUsername())));
        return true;
    }

    public boolean acceptFriendRequest(Player player, Player sender) throws Exception {
        UUID playerId = player.getUniqueId();
        UUID senderId = sender.getUniqueId();
        Set<UUID> requests = pendingRequests.get(playerId);
        if (requests == null || !requests.contains(senderId)) {
            player.sendMessage(CC.translateToComponent(NO_REQUEST.replace("%player%", sender.getUsername())));
            return false;
        }
        Set<UUID> playerFriends = friendLists.computeIfAbsent(playerId, k -> new HashSet<>());
        Set<UUID> senderFriends = friendLists.computeIfAbsent(senderId, k -> new HashSet<>());
        playerFriends.add(senderId);
        senderFriends.add(playerId);
        requests.remove(senderId);
        if (requests.isEmpty()) {
            pendingRequests.remove(playerId);
        }
        saveFriends(playerId);
        saveFriends(senderId);
        savePendingRequests(playerId);
        player.sendMessage(CC.translateToComponent(REQUEST_ACCEPTED.replace("%player%", sender.getUsername())));
        sender.sendMessage(CC.translateToComponent(ADDED.replace("%player%", player.getUsername())));
        return true;
    }

    public boolean denyFriendRequest(Player player, Player sender) throws Exception {
        UUID playerId = player.getUniqueId();
        UUID senderId = sender.getUniqueId();
        Set<UUID> requests = pendingRequests.get(playerId);
        if (requests == null || !requests.contains(senderId)) {
            player.sendMessage(CC.translateToComponent(NO_REQUEST.replace("%player%", sender.getUsername())));
            return false;
        }
        requests.remove(senderId);
        if (requests.isEmpty()) {
            pendingRequests.remove(playerId);
        }
        savePendingRequests(playerId);
        player.sendMessage(CC.translateToComponent(REQUEST_DENIED.replace("%player%", sender.getUsername())));
        sender.sendMessage(CC.translateToComponent(REQUEST_DENIED_BY.replace("%player%", player.getUsername())));
        return true;
    }

    public boolean removeFriend(Player player, Player target) throws Exception {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();
        Set<UUID> friends = friendLists.get(playerId);
        if (friends == null || !friends.contains(targetId)) {
            player.sendMessage(CC.translateToComponent(NOT_FRIEND.replace("%player%", target.getUsername())));
            return false;
        }
        friends.remove(targetId);
        Set<UUID> targetFriends = friendLists.get(targetId);
        if (targetFriends != null) {
            targetFriends.remove(playerId);
            saveFriends(targetId);
        }
        if (friends.isEmpty()) {
            friendLists.remove(playerId);
        }
        saveFriends(playerId);
        player.sendMessage(CC.translateToComponent(REMOVED.replace("%player%", target.getUsername())));
        if (target.getCurrentServer().isPresent()) {
            target.sendMessage(CC.translateToComponent(REMOVED_BY.replace("%player%", player.getUsername())));
        }
        return true;
    }

    public void listFriends(Player player) {
        UUID playerId = player.getUniqueId();
        Set<UUID> friends = friendLists.getOrDefault(playerId, Collections.emptySet());
        if (friends.isEmpty()) {
            player.sendMessage(CC.translateToComponent(LIST_EMPTY));
            return;
        }
        player.sendMessage(CC.translateToComponent(LIST_HEADER));
        for (UUID friendId : friends) {
            Optional<Player> friend = server.getPlayer(friendId);
            String status = friend.map(p -> "<green>Online <gray>(" + p.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("Unknown") + ")")
                    .orElse("<red>Offline");
            player.sendMessage(CC.translateToComponent(LIST_ENTRY
                    .replace("%player%", friend.map(Player::getUsername).orElse("Unknown"))
                    .replace("%status%", status)));
        }
    }

    public boolean joinFriend(Player player, Player target) {
        UUID targetId = target.getUniqueId();
        Set<UUID> friends = friendLists.get(player.getUniqueId());
        if (friends == null || !friends.contains(targetId)) {
            player.sendMessage(CC.translateToComponent(NOT_FRIEND.replace("%player%", target.getUsername())));
            return false;
        }
        if (!target.getCurrentServer().isPresent()) {
            player.sendMessage(CC.translateToComponent(OFFLINE.replace("%player%", target.getUsername())));
            return false;
        }
        player.createConnectionRequest(target.getCurrentServer().get().getServer()).fireAndForget();
        player.sendMessage(CC.translateToComponent(JOINED.replace("%player%", target.getUsername())));
        return true;
    }

    public void notifyFriendConnect(Player player) {
        if (!configManager.getConfig().node("friend", "notify-connect").getBoolean(true)) {
            return;
        }
        UUID playerId = player.getUniqueId();
        for (Map.Entry<UUID, Set<UUID>> entry : friendLists.entrySet()) {
            if (entry.getValue().contains(playerId)) {
                server.getPlayer(entry.getKey()).ifPresent(friend ->
                        friend.sendMessage(CC.translateToComponent(CONNECTED
                                .replace("%player%", player.getUsername())
                                .replace("%server%", player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("Unknown")))));
            }
        }
    }

    public void notifyFriendDisconnect(Player player) {
        if (!configManager.getConfig().node("friend", "notify-disconnect").getBoolean(true)) {
            return;
        }
        UUID playerId = player.getUniqueId();
        for (Map.Entry<UUID, Set<UUID>> entry : friendLists.entrySet()) {
            if (entry.getValue().contains(playerId)) {
                server.getPlayer(entry.getKey()).ifPresent(friend ->
                        friend.sendMessage(CC.translateToComponent(DISCONNECTED
                                .replace("%player%", player.getUsername()))));
            }
        }
    }

    public void loadFriends() {
        try {
            ConfigurationNode friendsConfig = configManager.getConfig();
            ConfigurationNode players = friendsConfig.node("players");
            if (players.empty()) {
                return;
            }
            for (Object playerIdObj : players.childrenMap().keySet()) {
                try {
                    UUID uuid = UUID.fromString(playerIdObj.toString());
                    List<String> friendIds = players.node(playerIdObj, "friends").getList(String.class, Collections.emptyList());
                    Set<UUID> friends = friendIds.stream()
                            .map(UUID::fromString)
                            .collect(Collectors.toSet());
                    friendLists.put(uuid, friends);
                    List<String> requestIds = players.node(playerIdObj, "pending-requests").getList(String.class, Collections.emptyList());
                    Set<UUID> requests = requestIds.stream()
                            .map(UUID::fromString)
                            .collect(Collectors.toSet());
                    if (!requests.isEmpty()) {
                        pendingRequests.put(uuid, requests);
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (Exception e) {
            server.getConsoleCommandSource().sendMessage(CC.translateToComponent("<red>Error loading friends: " + e.getMessage()));
        }
    }

    private void saveFriends(UUID playerId) throws Exception {
        ConfigurationNode friendsConfig = configManager.getConfig();
        Set<UUID> friends = friendLists.get(playerId);
        if (friends == null || friends.isEmpty()) {
            friendsConfig.node("players", playerId.toString(), "friends").set(null);
        } else {
            List<String> friendIds = friends.stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());
            friendsConfig.node("players", playerId.toString(), "friends").setList(String.class, friendIds);
        }
        configManager.saveConfig();
    }

    private void savePendingRequests(UUID playerId) throws Exception {
        ConfigurationNode friendsConfig = configManager.getConfig();
        Set<UUID> requests = pendingRequests.get(playerId);
        if (requests == null || requests.isEmpty()) {
            friendsConfig.node("players", playerId.toString(), "pending-requests").set(null);
        } else {
            List<String> requestIds = requests.stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());
            friendsConfig.node("players", playerId.toString(), "pending-requests").setList(String.class, requestIds);
        }
        configManager.saveConfig();
    }
}