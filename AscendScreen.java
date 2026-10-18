package com.ascend.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.net.URI;
import java.util.Locale;

public final class AscendScreen extends Screen {
    private static final int BLUE = 0xFF43B8FF;
    private static final int PANEL = 0xE8172638;
    private static final int ROW = 0xD91D3045;
    private Screen parent;

    public AscendScreen() { super(Text.literal("ASCEND")); }
    private void open(Screen screen) { MinecraftClient.getInstance().setScreen(screen); }
    private void panel(DrawContext c, int x, int y, int w, int h) {
        c.fill(x-2,y-2,x+w+2,y+h+2,BLUE); c.fill(x,y,x+w,y+h,PANEL);
        c.fill(x+1,y+1,x+w-1,y+2,0xFFB9E7FF);
    }
    private void label(DrawContext c, String s, int x, int y, int color) { c.drawTextWithShadow(textRenderer, Text.literal(s), x, y, color); }
    private ButtonWidget button(String text, int x, int y, int w, int h, Runnable action) {
        return ButtonWidget.builder(Text.literal(text), b -> action.run()).dimensions(x,y,w,h).build();
    }
    @Override protected void init() {
        int w = 280, x = (width-w)/2, y = Math.max(62,(height-210)/2);
        addDrawableChild(button("Ascend Discord",x,y,w,34,() -> { Util.getOperatingSystem().open(DISCORD_SAFE()); }));
        addDrawableChild(button("Clan Mitglieder",x,y+43,w,34,() -> open(new MembersScreen())));
        addDrawableChild(button("Ascend Clan Livestreams",x,y+86,w,34,() -> open(new StreamsScreen())));
        addDrawableChild(button("Auktion",x,y+129,w,34,() -> open(new AuctionScreen())));
    }
    private String DISCORD_SAFE() { return AscendClient.DISCORD; }
    @Override public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        renderBackground(c, mouseX, mouseY, delta);
        int w=330,h=250,x=(width-w)/2,y=(height-h)/2;
        panel(c,x,y,w,h); c.drawCenteredTextWithShadow(textRenderer, Text.literal("ASCEND"), width/2, y+20, 0xFF72C7FF);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("CLAN CLIENT  •  K"), width/2, y+44, 0xFFCFD8E4);
        super.render(c,mouseX,mouseY,delta);
    }

    private abstract class ChildScreen extends Screen {
        ChildScreen(String title) { super(Text.literal(title)); }
        void frame(DrawContext c, int mx, int my, float d, String heading) {
            renderBackground(c,mx,my,d); int w=Math.min(470,width-28), h=Math.min(310,height-28), x=(width-w)/2,y=(height-h)/2;
            panel(c,x,y,w,h); label(c,heading,x+14,y+12,0xFFFFFFFF); label(c,"ASCEND  /  CLIENT",x+14,y+h-20,0xFF8FCFFF);
        }
        void back() { open(new AscendScreen()); }
        @Override public void render(DrawContext c,int mx,int my,float d) { frame(c,mx,my,d,title.getString()); super.render(c,mx,my,d); }
    }

    private final class MembersScreen extends ChildScreen {
        MembersScreen(){super("Ascend Clan Mitglieder");}
        @Override protected void init(){ int x=width/2-130,y=height/2+80; addDrawableChild(button("/Clan info senden",x,y,150,24,()->{
            if(MinecraftClient.getInstance().player!=null) MinecraftClient.getInstance().player.networkHandler.sendChatCommand("Clan info");
            AscendClient.say("ASCEND: /Clan info wurde gesendet. Werte erscheinen nur, wenn die Chatantwort erkannt wird.");
        })); addDrawableChild(button("Zurück",x+160,y,100,24,this::back)); }
        @Override public void render(DrawContext c,int mx,int my,float d){super.render(c,mx,my,d); int x=width/2-205,y=height/2-55;
            label(c,"Mitglieder: "+AscendClient.DATA.clanMembers,x,y,0xFFFFFFFF);
            label(c,"Co-Owner: "+AscendClient.DATA.coOwners,x,y+28,0xFF43B8FF);
            label(c,"Moderatoren: "+AscendClient.DATA.moderators,x,y+56,0xFF43B8FF);
            label(c,"Hinweis: HugoSMP-Chatformat kann abweichen.",x,y+91,0xFFB6C2D0);
        }
    }

    private final class StreamsScreen extends ChildScreen {
        private TextFieldWidget urlField;
        StreamsScreen(){super("Ascend Clan Livestreams");}
        @Override protected void init(){int w=Math.min(430,width-40),x=(width-w)/2,y=height/2-78;
            urlField=new TextFieldWidget(textRenderer,x,y,w-4,22,Text.literal("Twitch- oder YouTube-Link")); urlField.setPlaceholder(Text.literal("https://twitch.tv/deinname")); addDrawableChild(urlField);
            addDrawableChild(button("Link speichern",x,y+29,130,24,()->saveStream()));
            addDrawableChild(button("Zurück",x+140,y+29,100,24,this::back));
            addDrawableChild(button("Streams aktualisieren",x+250,y+29,Math.min(175,w-250),24,()->{}));
        }
        private void saveStream(){String u=urlField.getText().trim(); if(!(u.startsWith("https://twitch.tv/")||u.startsWith("https://www.twitch.tv/")||u.startsWith("https://youtube.com/")||u.startsWith("https://www.youtube.com/")||u.startsWith("https://youtu.be/"))){AscendClient.say("ASCEND: Bitte einen gültigen Twitch- oder YouTube-https-Link eingeben.");return;}
            String p=MinecraftClient.getInstance().player==null?"Spieler":MinecraftClient.getInstance().player.getName().getString(); AscendClient.DATA.streams.removeIf(s->s.player.equalsIgnoreCase(p)); AscendClient.DATA.streams.add(new AscendData.StreamEntry(p,u)); AscendClient.DATA.save(); urlField.setText(""); }
        @Override public void render(DrawContext c,int mx,int my,float d){super.render(c,mx,my,d);int x=width/2-205,y=height/2-30;label(c,"Lokal gespeicherte Streams:",x,y,0xFFFFFFFF);int i=0;
            for(AscendData.StreamEntry s:AscendClient.DATA.streams){if(i>=5)break;int yy=y+20+i*25;c.fill(x-4,yy-3,x+405,yy+18,ROW);label(c,s.player+": "+trim(s.url,43),x,yy,0xFFCFD9E5);i++;}
            if(AscendClient.DATA.streams.isEmpty())label(c,"Noch keine Links gespeichert.",x,y+25,0xFFB6C2D0);
            // Buttons are placed in the row below each entry by the separate list in a compact way.
            int by=height/2+92; for(int j=0;j<Math.min(AscendClient.DATA.streams.size(),3);j++){final String u=AscendClient.DATA.streams.get(j).url; addOpenButtonOnce(j,u,by);}
        }
        private void addOpenButtonOnce(int index,String url,int y){String key="stream-open-"+index; if(children().stream().noneMatch(e->e instanceof ButtonWidget bw && bw.getMessage().getString().equals("Öffnen #"+(index+1)))) addDrawableChild(button("Öffnen #"+(index+1),width/2+105,y+index*23,95,20,()->Util.getOperatingSystem().open(url)));}
        private String trim(String s,int n){return s.length()>n?s.substring(0,n-3)+"...":s;}
    }

    private final class AuctionScreen extends ChildScreen {
        private TextFieldWidget bidField; private int duration=60;
        AuctionScreen(){super("Auktion erstellen");}
        @Override protected void init(){int x=width/2-205,y=height/2-75; ItemStack stack=MinecraftClient.getInstance().player==null?ItemStack.EMPTY:MinecraftClient.getInstance().player.getMainHandStack();
            addDrawableChild(button("Dauer: 30s",x,y+30,90,23,()->duration=30));
            addDrawableChild(button("1 Min",x+96,y+30,70,23,()->duration=60));
            addDrawableChild(button("1:30 Min",x+172,y+30,80,23,()->duration=90));
            addDrawableChild(button("2 Min",x+258,y+30,70,23,()->duration=120));
            bidField=new TextFieldWidget(textRenderer,x,y+3,150,20,Text.literal("Mindestgebot"));bidField.setText("1000");addDrawableChild(bidField);
            addDrawableChild(button("Auktion starten",x,y+65,180,26,()->start()));
            addDrawableChild(button("Aktive Auktion",x+190,y+65,140,26,()->open(new ActiveAuctionScreen())));
            addDrawableChild(button("Zurück",x,y+100,100,23,this::back));
        }
        private void start(){try{long min=Long.parseLong(bidField.getText().trim());if(min<0)throw new NumberFormatException();ItemStack item=MinecraftClient.getInstance().player==null?ItemStack.EMPTY:MinecraftClient.getInstance().player.getMainHandStack();if(item.isEmpty()){AscendClient.say("ASCEND: Halte zuerst ein Item in der Hand.");return;}AscendClient.DATA.startAuction(item.getName().getString(),min,duration);AscendClient.say("ASCEND: Lokale Auktion gestartet. Andere Mod-Nutzer sehen diese Auktion nicht automatisch.");open(new ActiveAuctionScreen());}catch(NumberFormatException e){AscendClient.say("ASCEND: Mindestgebot muss eine gültige Zahl sein.");}}
        @Override public void render(DrawContext c,int mx,int my,float d){super.render(c,mx,my,d);int x=width/2-205,y=height/2-90;ItemStack s=MinecraftClient.getInstance().player==null?ItemStack.EMPTY:MinecraftClient.getInstance().player.getMainHandStack();label(c,"Aktuelles Item: "+(s.isEmpty()?"(nichts gehalten)":s.getName().getString()),x,y,0xFF75D3FF);label(c,"Wähle Mindestgebot und Dauer.",x,y+17,0xFFD7E2EF);}
    }

    private final class ActiveAuctionScreen extends ChildScreen {
        ActiveAuctionScreen(){super("Aktive Auktion");}
        @Override protected void init(){addDrawableChild(button("Zurück",width/2-50,height/2+105,100,23,()->open(new AuctionScreen())));}
        @Override public void render(DrawContext c,int mx,int my,float d){super.render(c,mx,my,d);int x=width/2-205,y=height/2-65;
            if(!AscendClient.DATA.auctionActive){label(c,"Keine aktive lokale Auktion.",x,y,0xFFFFD080);label(c,"Gebote werden nur aus erkannten Chat-Zahlungen abgeleitet.",x,y+27,0xFFD7E2EF);}
            else{label(c,"Item: "+AscendClient.DATA.auctionItem,x,y,0xFF75D3FF);label(c,"Verkäufer: "+AscendClient.DATA.auctionOwner,x,y+23,0xFFFFFFFF);label(c,"Mindestgebot: "+AscendClient.DATA.minimumBid,x,y+46,0xFFFFFFFF);label(c,"Verbleibend: "+AscendClient.DATA.remainingSeconds()+" s",x,y+69,0xFF60FF9A);label(c,"Höchstes erkanntes Gebot: "+AscendClient.DATA.highestBid,x,y+92,0xFF60FF9A);}
            label(c,"Letzte erkannte Zahlung: "+AscendClient.DATA.lastPayment,x,y+125,0xFFB9C8D8);
        }
    }
}
