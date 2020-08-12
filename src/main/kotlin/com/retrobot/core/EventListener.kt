package com.retrobot.core

import com.retrobot.core.data.GuildSettingsRepository
import com.retrobot.core.util.Logger
import com.retrobot.core.util.removePrefixIgnoreCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.*
import net.dv8tion.jda.api.events.channel.category.CategoryCreateEvent
import net.dv8tion.jda.api.events.channel.category.CategoryDeleteEvent
import net.dv8tion.jda.api.events.channel.category.GenericCategoryEvent
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdateNameEvent
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdatePositionEvent
import net.dv8tion.jda.api.events.channel.category.update.GenericCategoryUpdateEvent
import net.dv8tion.jda.api.events.channel.priv.PrivateChannelCreateEvent
import net.dv8tion.jda.api.events.channel.priv.PrivateChannelDeleteEvent
import net.dv8tion.jda.api.events.channel.store.GenericStoreChannelEvent
import net.dv8tion.jda.api.events.channel.store.StoreChannelCreateEvent
import net.dv8tion.jda.api.events.channel.store.StoreChannelDeleteEvent
import net.dv8tion.jda.api.events.channel.store.update.GenericStoreChannelUpdateEvent
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdateNameEvent
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdatePositionEvent
import net.dv8tion.jda.api.events.channel.text.GenericTextChannelEvent
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent
import net.dv8tion.jda.api.events.channel.text.update.*
import net.dv8tion.jda.api.events.channel.voice.GenericVoiceChannelEvent
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelCreateEvent
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent
import net.dv8tion.jda.api.events.channel.voice.update.*
import net.dv8tion.jda.api.events.emote.EmoteAddedEvent
import net.dv8tion.jda.api.events.emote.EmoteRemovedEvent
import net.dv8tion.jda.api.events.emote.GenericEmoteEvent
import net.dv8tion.jda.api.events.emote.update.EmoteUpdateNameEvent
import net.dv8tion.jda.api.events.emote.update.EmoteUpdateRolesEvent
import net.dv8tion.jda.api.events.emote.update.GenericEmoteUpdateEvent
import net.dv8tion.jda.api.events.guild.*
import net.dv8tion.jda.api.events.guild.invite.GenericGuildInviteEvent
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent
import net.dv8tion.jda.api.events.guild.member.*
import net.dv8tion.jda.api.events.guild.member.update.GenericGuildMemberUpdateEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.events.guild.override.GenericPermissionOverrideEvent
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideCreateEvent
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideDeleteEvent
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideUpdateEvent
import net.dv8tion.jda.api.events.guild.update.*
import net.dv8tion.jda.api.events.guild.voice.*
import net.dv8tion.jda.api.events.http.HttpRequestEvent
import net.dv8tion.jda.api.events.message.*
import net.dv8tion.jda.api.events.message.guild.*
import net.dv8tion.jda.api.events.message.guild.react.*
import net.dv8tion.jda.api.events.message.priv.*
import net.dv8tion.jda.api.events.message.priv.react.GenericPrivateMessageReactionEvent
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionRemoveEvent
import net.dv8tion.jda.api.events.message.react.*
import net.dv8tion.jda.api.events.role.GenericRoleEvent
import net.dv8tion.jda.api.events.role.RoleCreateEvent
import net.dv8tion.jda.api.events.role.RoleDeleteEvent
import net.dv8tion.jda.api.events.role.update.*
import net.dv8tion.jda.api.events.self.*
import net.dv8tion.jda.api.events.user.GenericUserEvent
import net.dv8tion.jda.api.events.user.UserActivityEndEvent
import net.dv8tion.jda.api.events.user.UserActivityStartEvent
import net.dv8tion.jda.api.events.user.UserTypingEvent
import net.dv8tion.jda.api.events.user.update.*
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Handles all JDA [Event]s.
 *
 * @param bot The [Bot] to dispatch [Event]s to.
 */
class EventListener(private val bot: Bot): ListenerAdapter(), KoinComponent {

    private val scope = CoroutineScope(Job() + Dispatchers.Default)
    private val guildSettingsRepo: GuildSettingsRepository by inject()


    override fun onGenericEvent(event: GenericEvent) {}
    override fun onGenericUpdate(event: UpdateEvent<*, *>) {}
    override fun onRawGateway(event: RawGatewayEvent) {}
    override fun onGatewayPing(event: GatewayPingEvent) {}

    // JDA Events
    override fun onReady(event: ReadyEvent) {}
    override fun onResume(event: ResumedEvent) {}
    override fun onReconnect(event: ReconnectedEvent) {}
    override fun onDisconnect(event: DisconnectEvent) {}
    override fun onShutdown(event: ShutdownEvent) {}
    override fun onStatusChange(event: StatusChangeEvent) {}
    override fun onException(event: ExceptionEvent) {}

    // User Events
    override fun onUserUpdateName(event: UserUpdateNameEvent) {}
    override fun onUserUpdateDiscriminator(event: UserUpdateDiscriminatorEvent) {}
    override fun onUserUpdateAvatar(event: UserUpdateAvatarEvent) {}
    override fun onUserUpdateOnlineStatus(event: UserUpdateOnlineStatusEvent) {}
    override fun onUserUpdateActivityOrder(event: UserUpdateActivityOrderEvent) {}
    override fun onUserUpdateFlags(event: UserUpdateFlagsEvent) {}
    override fun onUserTyping(event: UserTypingEvent) {}
    override fun onUserActivityStart(event: UserActivityStartEvent) {}
    override fun onUserActivityEnd(event: UserActivityEndEvent) {}

    // Self Events. Fires only in relation to the currently logged in account.
    override fun onSelfUpdateAvatar(event: SelfUpdateAvatarEvent) {}
    override fun onSelfUpdateMFA(event: SelfUpdateMFAEvent) {}
    override fun onSelfUpdateName(event: SelfUpdateNameEvent) {}
    override fun onSelfUpdateVerified(event: SelfUpdateVerifiedEvent) {}

    // Message Events
    // Guild (TextChannel) Message Events
    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        Logger.log(event)

        // Prevents message recognition if author is a bot
        if (event.author.isBot) return
        scope.launch {
            val guildSettings = guildSettingsRepo.getGuildSettings(event.guild.id)
            if (event.message.contentRaw.startsWith(guildSettings.commandPrefix, true)) {
                val message = event.message.contentRaw.removePrefixIgnoreCase(guildSettings.commandPrefix)
                for (command in bot.commandSet) {
                    if (command.handle(bot, event, message, guildSettings)) return@launch
                }
            }
        }
    }
    override fun onGuildMessageUpdate(event: GuildMessageUpdateEvent) {}
    override fun onGuildMessageDelete(event: GuildMessageDeleteEvent) {}
    override fun onGuildMessageEmbed(event: GuildMessageEmbedEvent) {}
    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        Logger.log(event)
        scope.launch {
            val guildSettings = guildSettingsRepo.getGuildSettings(event.guild.id)
            bot.reactionHandler.onGuildMessageReactionAdd(bot, event, guildSettings)
        }
    }
    override fun onGuildMessageReactionRemove(event: GuildMessageReactionRemoveEvent) {
        Logger.log(event)
        scope.launch {
            val guildSettings = guildSettingsRepo.getGuildSettings(event.guild.id)
            bot.reactionHandler.onGuildMessageReactionRemove(bot, event, guildSettings)
        }
    }
    override fun onGuildMessageReactionRemoveAll(event: GuildMessageReactionRemoveAllEvent) {
        Logger.log(event)
        scope.launch {
            val guildSettings = guildSettingsRepo.getGuildSettings(event.guild.id)
            bot.reactionHandler.onGuildMessageReactionRemoveAll(bot, event, guildSettings)
        }
    }
    override fun onGuildMessageReactionRemoveEmote(event: GuildMessageReactionRemoveEmoteEvent) {}

    // Private Message Events
    override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {}
    override fun onPrivateMessageUpdate(event: PrivateMessageUpdateEvent) {}
    override fun onPrivateMessageDelete(event: PrivateMessageDeleteEvent) {}
    override fun onPrivateMessageEmbed(event: PrivateMessageEmbedEvent) {}
    override fun onPrivateMessageReactionAdd(event: PrivateMessageReactionAddEvent) {}
    override fun onPrivateMessageReactionRemove(event: PrivateMessageReactionRemoveEvent) {}

    // Combined Message Events (Combines Guild and Private message into 1 event)
    override fun onMessageReceived(event: MessageReceivedEvent) {}
    override fun onMessageUpdate(event: MessageUpdateEvent) {}
    override fun onMessageDelete(event: MessageDeleteEvent) {}
    override fun onMessageBulkDelete(event: MessageBulkDeleteEvent) {}
    override fun onMessageEmbed(event: MessageEmbedEvent) {}
    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {}
    override fun onMessageReactionRemove(event: MessageReactionRemoveEvent) {}
    override fun onMessageReactionRemoveAll(event: MessageReactionRemoveAllEvent) {}
    override fun onMessageReactionRemoveEmote(event: MessageReactionRemoveEmoteEvent) {}

    // PermissionOverride Events
    override fun onPermissionOverrideDelete(event: PermissionOverrideDeleteEvent) {}
    override fun onPermissionOverrideUpdate(event: PermissionOverrideUpdateEvent) {}
    override fun onPermissionOverrideCreate(event: PermissionOverrideCreateEvent) {}

    // StoreChannel Events
    override fun onStoreChannelDelete(event: StoreChannelDeleteEvent) {}
    override fun onStoreChannelUpdateName(event: StoreChannelUpdateNameEvent) {}
    override fun onStoreChannelUpdatePosition(event: StoreChannelUpdatePositionEvent) {}
    override fun onStoreChannelCreate(event: StoreChannelCreateEvent) {}

    // TextChannel Events
    override fun onTextChannelDelete(event: TextChannelDeleteEvent) {}
    override fun onTextChannelUpdateName(event: TextChannelUpdateNameEvent) {}
    override fun onTextChannelUpdateTopic(event: TextChannelUpdateTopicEvent) {}
    override fun onTextChannelUpdatePosition(event: TextChannelUpdatePositionEvent) {}
    override fun onTextChannelUpdateNSFW(event: TextChannelUpdateNSFWEvent) {}
    override fun onTextChannelUpdateParent(event: TextChannelUpdateParentEvent) {}
    override fun onTextChannelUpdateSlowmode(event: TextChannelUpdateSlowmodeEvent) {}
    override fun onTextChannelCreate(event: TextChannelCreateEvent) {}

    // VoiceChannel Events
    override fun onVoiceChannelDelete(event: VoiceChannelDeleteEvent) {}
    override fun onVoiceChannelUpdateName(event: VoiceChannelUpdateNameEvent) {}
    override fun onVoiceChannelUpdatePosition(event: VoiceChannelUpdatePositionEvent) {}
    override fun onVoiceChannelUpdateUserLimit(event: VoiceChannelUpdateUserLimitEvent) {}
    override fun onVoiceChannelUpdateBitrate(event: VoiceChannelUpdateBitrateEvent) {}
    override fun onVoiceChannelUpdateParent(event: VoiceChannelUpdateParentEvent) {}
    override fun onVoiceChannelCreate(event: VoiceChannelCreateEvent) {}

    // Category Events
    override fun onCategoryDelete(event: CategoryDeleteEvent) {}
    override fun onCategoryUpdateName(event: CategoryUpdateNameEvent) {}
    override fun onCategoryUpdatePosition(event: CategoryUpdatePositionEvent) {}
    override fun onCategoryCreate(event: CategoryCreateEvent) {}

    // PrivateChannel Events
    override fun onPrivateChannelCreate(event: PrivateChannelCreateEvent) {}
    override fun onPrivateChannelDelete(event: PrivateChannelDeleteEvent) {}

    // Guild Events
    override fun onGuildReady(event: GuildReadyEvent) {}
    override fun onGuildJoin(event: GuildJoinEvent) {
        Logger.log(event)
        scope.launch {
            val guild = event.guild
            val guildSettings = guildSettingsRepo.getGuildSettings(guild.id)

            if (guildSettings.isBanned) {
                guild.leave().queue()
            } else {
                guild.selfMember.modifyNickname(guildSettings.botNickname).queue()
                val description = "Thanks for adding me to your guild!\n" +
                        "To see what I can do you can type the command `${guildSettings.commandPrefix}help`."
                val helloMessage = EmbedBuilder()
                        .setColor(guildSettings.botHighlightColor)
                        .setTitle("${guildSettings.botNickname} has arrived!")
                        .setDescription(description)
                        .build()
                for (textChannel in guild.textChannels) {
                    if (textChannel.canTalk()) {
                        textChannel.sendMessage(helloMessage).queue()
                        break
                    }
                }
            }
        }
    }
    override fun onGuildLeave(event: GuildLeaveEvent) { Logger.log(event) }
    override fun onGuildAvailable(event: GuildAvailableEvent) {}
    override fun onGuildUnavailable(event: GuildUnavailableEvent) {}
    override fun onUnavailableGuildJoined(event: UnavailableGuildJoinedEvent) {}
    override fun onUnavailableGuildLeave(event: UnavailableGuildLeaveEvent) {}
    override fun onGuildBan(event: GuildBanEvent) {}
    override fun onGuildUnban(event: GuildUnbanEvent) {}
    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {}

    // Guild Update Events
    override fun onGuildUpdateAfkChannel(event: GuildUpdateAfkChannelEvent) {}
    override fun onGuildUpdateSystemChannel(event: GuildUpdateSystemChannelEvent) {}
    override fun onGuildUpdateAfkTimeout(event: GuildUpdateAfkTimeoutEvent) {}
    override fun onGuildUpdateExplicitContentLevel(event: GuildUpdateExplicitContentLevelEvent) {}
    override fun onGuildUpdateIcon(event: GuildUpdateIconEvent) {}
    override fun onGuildUpdateMFALevel(event: GuildUpdateMFALevelEvent) {}
    override fun onGuildUpdateName(event: GuildUpdateNameEvent) {}
    override fun onGuildUpdateNotificationLevel(event: GuildUpdateNotificationLevelEvent) {}
    override fun onGuildUpdateOwner(event: GuildUpdateOwnerEvent) {}
    override fun onGuildUpdateRegion(event: GuildUpdateRegionEvent) {}
    override fun onGuildUpdateSplash(event: GuildUpdateSplashEvent) {}
    override fun onGuildUpdateVerificationLevel(event: GuildUpdateVerificationLevelEvent) {}
    override fun onGuildUpdateFeatures(event: GuildUpdateFeaturesEvent) {}
    override fun onGuildUpdateVanityCode(event: GuildUpdateVanityCodeEvent) {}
    override fun onGuildUpdateBanner(event: GuildUpdateBannerEvent) {}
    override fun onGuildUpdateDescription(event: GuildUpdateDescriptionEvent) {}
    override fun onGuildUpdateBoostTier(event: GuildUpdateBoostTierEvent) {}
    override fun onGuildUpdateBoostCount(event: GuildUpdateBoostCountEvent) {}
    override fun onGuildUpdateMaxMembers(event: GuildUpdateMaxMembersEvent) {}
    override fun onGuildUpdateMaxPresences(event: GuildUpdateMaxPresencesEvent) {}

    // Guild Invite Events
    override fun onGuildInviteCreate(event: GuildInviteCreateEvent) {}
    override fun onGuildInviteDelete(event: GuildInviteDeleteEvent) {}

    // Guild Member Events
    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {}
    override fun onGuildMemberRoleAdd(event: GuildMemberRoleAddEvent) {}
    override fun onGuildMemberRoleRemove(event: GuildMemberRoleRemoveEvent) {}

    // Guild Member Update Events
    override fun onGuildMemberUpdateNickname(event: GuildMemberUpdateNicknameEvent) {}
    override fun onGuildMemberUpdateBoostTime(event: GuildMemberUpdateBoostTimeEvent) {}

    // Guild Voice Events
    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {}
    override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {}
    override fun onGuildVoiceMove(event: GuildVoiceMoveEvent) {}
    override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {}
    override fun onGuildVoiceMute(event: GuildVoiceMuteEvent) {}
    override fun onGuildVoiceDeafen(event: GuildVoiceDeafenEvent) {}
    override fun onGuildVoiceGuildMute(event: GuildVoiceGuildMuteEvent) {}
    override fun onGuildVoiceGuildDeafen(event: GuildVoiceGuildDeafenEvent) {}
    override fun onGuildVoiceSelfMute(event: GuildVoiceSelfMuteEvent) {}
    override fun onGuildVoiceSelfDeafen(event: GuildVoiceSelfDeafenEvent) {}
    override fun onGuildVoiceSuppress(event: GuildVoiceSuppressEvent) {}
    override fun onGuildVoiceStream(event: GuildVoiceStreamEvent) {}

    // Role events
    override fun onRoleCreate(event: RoleCreateEvent) {}
    override fun onRoleDelete(event: RoleDeleteEvent) {}

    // Role Update Events
    override fun onRoleUpdateColor(event: RoleUpdateColorEvent) {}
    override fun onRoleUpdateHoisted(event: RoleUpdateHoistedEvent) {}
    override fun onRoleUpdateMentionable(event: RoleUpdateMentionableEvent) {}
    override fun onRoleUpdateName(event: RoleUpdateNameEvent) {}
    override fun onRoleUpdatePermissions(event: RoleUpdatePermissionsEvent) {}
    override fun onRoleUpdatePosition(event: RoleUpdatePositionEvent) {}

    // Emote Events
    override fun onEmoteAdded(event: EmoteAddedEvent) {}
    override fun onEmoteRemoved(event: EmoteRemovedEvent) {}

    // Emote Update Events
    override fun onEmoteUpdateName(event: EmoteUpdateNameEvent) {}
    override fun onEmoteUpdateRoles(event: EmoteUpdateRolesEvent) {}

    // Debug Events
    override fun onHttpRequest(event: HttpRequestEvent) {}

    // Generic Events
    override fun onGenericMessage(event: GenericMessageEvent) {}
    override fun onGenericMessageReaction(event: GenericMessageReactionEvent) {}
    override fun onGenericGuildMessage(event: GenericGuildMessageEvent) {}
    override fun onGenericGuildMessageReaction(event: GenericGuildMessageReactionEvent) {}
    override fun onGenericPrivateMessage(event: GenericPrivateMessageEvent) {}
    override fun onGenericPrivateMessageReaction(event: GenericPrivateMessageReactionEvent) {}
    override fun onGenericUser(event: GenericUserEvent) {}
    override fun onGenericUserPresence(event: GenericUserPresenceEvent) {}
    override fun onGenericSelfUpdate(event: GenericSelfUpdateEvent<*>) {}
    override fun onGenericStoreChannel(event: GenericStoreChannelEvent) {}
    override fun onGenericStoreChannelUpdate(event: GenericStoreChannelUpdateEvent<*>) {}
    override fun onGenericTextChannel(event: GenericTextChannelEvent) {}
    override fun onGenericTextChannelUpdate(event: GenericTextChannelUpdateEvent<*>) {}
    override fun onGenericVoiceChannel(event: GenericVoiceChannelEvent) {}
    override fun onGenericVoiceChannelUpdate(event: GenericVoiceChannelUpdateEvent<*>) {}
    override fun onGenericCategory(event: GenericCategoryEvent) {}
    override fun onGenericCategoryUpdate(event: GenericCategoryUpdateEvent<*>) {}
    override fun onGenericGuild(event: GenericGuildEvent) {}
    override fun onGenericGuildUpdate(event: GenericGuildUpdateEvent<*>) {}
    override fun onGenericGuildInvite(event: GenericGuildInviteEvent) {}
    override fun onGenericGuildMember(event: GenericGuildMemberEvent) {}
    override fun onGenericGuildMemberUpdate(event: GenericGuildMemberUpdateEvent<*>) {}
    override fun onGenericGuildVoice(event: GenericGuildVoiceEvent) {}
    override fun onGenericRole(event: GenericRoleEvent) {}
    override fun onGenericRoleUpdate(event: GenericRoleUpdateEvent<*>) {}
    override fun onGenericEmote(event: GenericEmoteEvent) {}
    override fun onGenericEmoteUpdate(event: GenericEmoteUpdateEvent<*>) {}
    override fun onGenericPermissionOverride(event: GenericPermissionOverrideEvent) {}
}