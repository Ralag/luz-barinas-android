#!/usr/bin/env python3
"""
Seed Firestore with full Luz Barinas data catalog:
- PAC Schedule & Matrix
- Sectors (All ~75+ neighborhoods and circuits categorized by Bloques A, B, C, D)
- Community Location Proposals
- Official PAC Broadcast Notice
- Recent Citizen Reports Feed
"""

import urllib.request
import json
import time
import re
import sys

API_KEY = "AIzaSyBG1xchQ_FnFI5JJMkiiToXsdo7wMQj1sc"
PROJECT_ID = "luzbarinas-6cabc"
BASE_URL = f"https://firestore.googleapis.com/v1/projects/{PROJECT_ID}/databases/(default)/documents"

def firestore_patch(collection_path, doc_id, fields):
    url = f"{BASE_URL}/{collection_path}/{doc_id}?key={API_KEY}"
    body = json.dumps({"fields": fields}).encode("utf-8")
    req = urllib.request.Request(url, data=body, method="PATCH")
    req.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(req) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except Exception as e:
        print(f"Error patching {collection_path}/{doc_id}: {e}")
        return None

def firestore_create(collection_path, doc_id, fields):
    url = f"{BASE_URL}/{collection_path}?documentId={doc_id}&key={API_KEY}"
    body = json.dumps({"fields": fields}).encode("utf-8")
    req = urllib.request.Request(url, data=body, method="POST")
    req.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(req) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        if e.code == 409: # Already exists, use patch
            return firestore_patch(collection_path, doc_id, fields)
        print(f"HTTPError {e.code} on {collection_path}/{doc_id}: {e.read().decode('utf-8')}")
        return None
    except Exception as e:
        print(f"Error creating {collection_path}/{doc_id}: {e}")
        return None

def to_firestore_value(v):
    if isinstance(v, str):
        return {"stringValue": v}
    elif isinstance(v, bool):
        return {"booleanValue": v}
    elif isinstance(v, int):
        return {"integerValue": str(v)}
    elif isinstance(v, float):
        return {"doubleValue": v}
    elif isinstance(v, list):
        return {"arrayValue": {"values": [to_firestore_value(x) for x in v]}}
    elif isinstance(v, dict):
        return {"mapValue": {"fields": {k: to_firestore_value(val) for k, val in v.items()}}}
    else:
        return {"stringValue": str(v)}

def to_firestore_fields(d):
    return {k: to_firestore_value(v) for k, v in d.items()}

# ==============================================================================
# DATA DEFINITIONS
# ==============================================================================

# 1. PAC Schedule & Matrix
PAC_SLOTS = [
    {"timeLabel": "03:00 a 07:00", "startHour": 3, "endHour": 7},
    {"timeLabel": "07:00 a 11:00", "startHour": 7, "endHour": 11},
    {"timeLabel": "11:00 a 15:00", "startHour": 11, "endHour": 15},
    {"timeLabel": "15:00 a 19:00", "startHour": 15, "endHour": 19},
    {"timeLabel": "19:00 a 23:00", "startHour": 19, "endHour": 23},
    {"timeLabel": "23:00 a 03:00", "startHour": 23, "endHour": 3}
]

PAC_MATRIX = [
    ["C", "B", "A", "C", "B", "A", "C"],
    ["A", "C", "B", "A", "C", "B", "A"],
    ["B", "A", "C", "B", "A", "C", "B"],
    ["C", "B", "A", "C", "B", "A", "C"],
    ["A", "C", "B", "A", "C", "B", "A"],
    ["B", "A", "C", "B", "A", "C", "B"]
]

SECTORS_BLOQUE_A = [
    "Alto Barinas I 34,5 kV", "Obispos 34,5 kV", "Guasimito", "Centro", "Sur",
    "Industrial", "Norte", "Raúl Leoni", "Las Palmas", "Primero Diciembre",
    "Pagueycito", "Progreso", "Don Simón", "El Real", "El Tambor", "Cdad Bolivia II",
    "Mijagua 34,5 kV", "Mirí", "Urb. Las Ingenieras I, II y III", "Los Pozones",
    "Urb. Don Samuel", "Alto Barinas Norte", "Urb. Los Próceres", "Urb. La Rosaleda",
    "Urb. Vista Hermosa", "Urb. Villas del Pilar", "Urb. Campo Móvil"
]

SECTORS_BLOQUE_B = [
    "Barinitas 34,5 kV", "Parangula", "Floresta", "Centro Norte", "Los Pinos",
    "Carolina", "Borburata", "Negro Primero", "Hormiga", "Cdad Varyna",
    "Cdad Tavacare", "San Silvestre", "Sta Ines Lucia", "Libertad", "Sta. Rosa",
    "Curbati", "Ticoporo", "Capitanejo 34,5 kV", "Santa Elena kV", "Barrio 5 de Julio",
    "Mi Jardín (Sectores 1, 2, 3)", "Ciudad Varyna Los Samanes", "Ciudad Varyna Bucares",
    "Ciudad Tavacare Sector A-C", "Ciudad Tavacare Sector D-G", "Palacio del Pan / Av. 23 de Enero"
]

SECTORS_BLOQUE_C = [
    "Expresa 34,5 kV", "Alto Barinas II 34,5 kV", "Socopo I", "Bum Bum", "Socopo II",
    "Cardenera", "Corocito", "Bolivar", "Esperanza", "Torunos", "Cdad Nutria 34,5 kV",
    "Dolores", "El Paguey", "Cdad Bolivia I", "Fundacea", "Estadio", "Barrio El Carmen",
    "Urb. La Cincuentena I, II y III", "Barrio La Paz", "Urb. Barinas II", "Urb. Barinas IV",
    "Alto Barinas Sur", "Casco Central / Plaza Bolívar", "Av. Medina Jiménez"
]

SECTORS_BLOQUE_D = [
    "Alto Barinas II 34,5 kV", "Obispos 34,5 kV", "Floresta", "Los Pinos", "Industrial",
    "Cardenera", "Corocito", "El Paguey", "Cdad Bolivia I", "Mijagua 34,5 kV",
    "Caaez 34,5 kV", "Boconoito", "Otopum-Pajen", "Canagua 34,5 kV", "Libertad",
    "Sector La Caramuca", "Sector Punta Gorda", "Quebrada Seca", "El Corozo"
]

# Detailed Sector Master Catalog with Parroquias and Circuits
MASTER_SECTORS = [
    # --- BLOQUE A ---
    {"name": "Alto Barinas Norte", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Don Samuel / Norte", "status": "NORMAL", "voltage": 118.5},
    {"name": "Urb. Don Samuel", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Don Samuel", "status": "NORMAL", "voltage": 117.8},
    {"name": "Urb. Los Próceres", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Norte", "status": "NORMAL", "voltage": 119.0},
    {"name": "Urb. Las Ingenieras I, II y III", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Norte", "status": "NORMAL", "voltage": 118.2},
    {"name": "Urb. La Rosaleda", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Alto Barinas I", "status": "NORMAL", "voltage": 118.0},
    {"name": "Urb. Vista Hermosa", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Don Samuel", "status": "NORMAL", "voltage": 117.5},
    {"name": "Urb. Villas del Pilar", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Alto Barinas I", "status": "NORMAL", "voltage": 118.0},
    {"name": "Urb. Campo Móvil", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Norte", "status": "NORMAL", "voltage": 118.0},
    {"name": "Urb. Raúl Leoni", "parroquia": "R. Ignacio Méndez", "block": "A", "circuit": "Circuito Raúl Leoni", "status": "NORMAL", "voltage": 117.0},
    {"name": "Los Pozones", "parroquia": "Rómulo Betancourt", "block": "A", "circuit": "Circuito Los Pozones", "status": "NORMAL", "voltage": 116.5},
    {"name": "Las Palmas", "parroquia": "Alto Barinas", "block": "A", "circuit": "Circuito Las Palmas", "status": "NORMAL", "voltage": 118.0},
    {"name": "Primero de Diciembre", "parroquia": "R. Ignacio Méndez", "block": "A", "circuit": "Circuito Mijagua", "status": "NORMAL", "voltage": 117.2},
    {"name": "Zona Industrial", "parroquia": "Torunos", "block": "A", "circuit": "Circuito Industrial", "status": "NORMAL", "voltage": 119.5},
    {"name": "Obispos 34,5 kV", "parroquia": "Municipio Obispos", "block": "A", "circuit": "Subestación Obispos", "status": "NORMAL", "voltage": 118.0},
    {"name": "Guasimito", "parroquia": "Obispos", "block": "A", "circuit": "Circuito Guasimito", "status": "NORMAL", "voltage": 116.0},
    {"name": "Ciudad Bolivia II", "parroquia": "Pedraza", "block": "A", "circuit": "Circuito Pedraza", "status": "NORMAL", "voltage": 115.5},

    # --- BLOQUE B ---
    {"name": "Barinitas 34,5 kV", "parroquia": "Bolívar", "block": "B", "circuit": "Subestación Barinitas", "status": "NORMAL", "voltage": 118.0},
    {"name": "Ciudad Varyna Los Samanes", "parroquia": "Alto Barinas", "block": "B", "circuit": "Circuito Cdad Varyna", "status": "NORMAL", "voltage": 117.5},
    {"name": "Ciudad Varyna Bucares", "parroquia": "Alto Barinas", "block": "B", "circuit": "Circuito Cdad Varyna", "status": "NORMAL", "voltage": 117.0},
    {"name": "Ciudad Tavacare Sector A-C", "parroquia": "Alto Barinas", "block": "B", "circuit": "Circuito Cdad Tavacare", "status": "NORMAL", "voltage": 116.8},
    {"name": "Ciudad Tavacare Sector D-G", "parroquia": "Alto Barinas", "block": "B", "circuit": "Circuito Cdad Tavacare", "status": "NORMAL", "voltage": 116.5},
    {"name": "Barrio 5 de Julio", "parroquia": "Corazón de Jesús", "block": "B", "circuit": "Circuito Carolina", "status": "NORMAL", "voltage": 117.0},
    {"name": "Mi Jardín (Sectores 1, 2, 3)", "parroquia": "R. Ignacio Méndez", "block": "B", "circuit": "Circuito Hormiga", "status": "NORMAL", "voltage": 116.0},
    {"name": "Urb. La Floresta", "parroquia": "Corazón de Jesús", "block": "B", "circuit": "Circuito Floresta", "status": "NORMAL", "voltage": 118.0},
    {"name": "Los Pinos", "parroquia": "Corazón de Jesús", "block": "B", "circuit": "Circuito Los Pinos", "status": "NORMAL", "voltage": 117.5},
    {"name": "Palacio del Pan / Av. 23 de Enero", "parroquia": "El Carmen", "block": "B", "circuit": "Circuito Centro Norte", "status": "NORMAL", "voltage": 118.2},
    {"name": "Barrio Negro Primero", "parroquia": "Corazón de Jesús", "block": "B", "circuit": "Circuito Negro Primero", "status": "NORMAL", "voltage": 116.5},
    {"name": "San Silvestre", "parroquia": "San Silvestre", "block": "B", "circuit": "Circuito Foráneo", "status": "NORMAL", "voltage": 115.0},
    {"name": "Santa Rosa", "parroquia": "Santa Rosa", "block": "B", "circuit": "Circuito Santa Rosa", "status": "NORMAL", "voltage": 115.0},

    # --- BLOQUE C ---
    {"name": "Urb. La Cincuentena I y II", "parroquia": "El Carmen", "block": "C", "circuit": "Circuito Corocito", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Urb. La Cincuentena III", "parroquia": "El Carmen", "block": "C", "circuit": "Circuito Corocito", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Barrio El Carmen", "parroquia": "El Carmen", "block": "C", "circuit": "Circuito Corocito", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Barrio La Paz", "parroquia": "Rómulo Betancourt", "block": "C", "circuit": "Circuito Esperanza", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Urb. Barinas II", "parroquia": "Corazón de Jesús", "block": "C", "circuit": "Circuito Corocito", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Urb. Barinas IV", "parroquia": "Corazón de Jesús", "block": "C", "circuit": "Circuito Corocito", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Alto Barinas Sur", "parroquia": "Alto Barinas", "block": "C", "circuit": "Circuito Alto Barinas II 34,5 kV", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Casco Central / Plaza Bolívar", "parroquia": "Barinas", "block": "C", "circuit": "Circuito Bolívar", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Av. Medina Jiménez", "parroquia": "Barinas", "block": "C", "circuit": "Circuito Estadio", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Socopó I", "parroquia": "Sucre", "block": "C", "circuit": "Subestación Socopó", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Bum Bum", "parroquia": "Sucre", "block": "C", "circuit": "Circuito Bum Bum", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Torunos", "parroquia": "Torunos", "block": "C", "circuit": "Circuito Torunos", "status": "SCHEDULED_OUTAGE", "voltage": 0.0},
    {"name": "Fundacea", "parroquia": "Barinas", "block": "C", "circuit": "Circuito Fundacea", "status": "IRREGULAR_OUTAGE", "voltage": 0.0},

    # --- BLOQUE D ---
    {"name": "Sector La Caramuca", "parroquia": "M. Palacio Fajardo", "block": "D", "circuit": "Circuito La Caramuca", "status": "NORMAL", "voltage": 117.0},
    {"name": "Sector Punta Gorda", "parroquia": "Corazón de Jesús", "block": "D", "circuit": "Circuito Punta Gorda", "status": "NORMAL", "voltage": 116.5},
    {"name": "Quebrada Seca", "parroquia": "M. Palacio Fajardo", "block": "D", "circuit": "Circuito Foráneo Sur", "status": "NORMAL", "voltage": 115.0},
    {"name": "El Corozo", "parroquia": "M. Palacio Fajardo", "block": "D", "circuit": "Circuito El Corozo", "status": "NORMAL", "voltage": 116.0},
    {"name": "Boconoito", "parroquia": "San Genaro", "block": "D", "circuit": "Circuito Boconoito", "status": "NORMAL", "voltage": 116.0},
    {"name": "Canagua 34,5 kV", "parroquia": "Pedraza", "block": "D", "circuit": "Circuito Canagua", "status": "NORMAL", "voltage": 115.8}
]

# Community location submissions for review in Tab 4
COMMUNITY_SUBMISSIONS = [
    {
        "id": "sec_community_cincuentena_etapa_4",
        "name": "Urb. La Cincuentena Etapa IV",
        "parroquia": "El Carmen",
        "block": "C",
        "circuitCode": "Circuito Corocito",
        "status": "NORMAL",
        "voltage": 118.0
    },
    {
        "id": "sec_community_barrio_el_cambio",
        "name": "Barrio El Cambio",
        "parroquia": "Rómulo Betancourt",
        "block": "C",
        "circuitCode": "Circuito Esperanza",
        "status": "NORMAL",
        "voltage": 118.0
    },
    {
        "id": "sec_community_las_colinas_alto_barinas",
        "name": "Conj. Res. Las Colinas",
        "parroquia": "Alto Barinas",
        "block": "A",
        "circuitCode": "Circuito Don Samuel",
        "status": "NORMAL",
        "voltage": 118.0
    },
    {
        "id": "sec_community_la_arenosa",
        "name": "Sector La Arenosa",
        "parroquia": "M. Palacio Fajardo",
        "block": "D",
        "circuitCode": "Circuito La Caramuca",
        "status": "NORMAL",
        "voltage": 118.0
    }
]

# Recent citizen reports for Tab 5 feed
CITIZEN_REPORTS = [
    {"sectorName": "Urb. Don Samuel", "hasPower": True, "reportType": "NORMAL", "voltage": 118.0, "deviceOrigin": "CitizenApp_Mobile"},
    {"sectorName": "Alto Barinas Norte", "hasPower": True, "reportType": "NORMAL", "voltage": 119.0, "deviceOrigin": "CitizenApp_Mobile"},
    {"sectorName": "Urb. La Cincuentena I y II", "hasPower": False, "reportType": "PAC", "voltage": 0.0, "deviceOrigin": "CitizenApp_Mobile"},
    {"sectorName": "Barrio El Carmen", "hasPower": False, "reportType": "PAC", "voltage": 0.0, "deviceOrigin": "CitizenApp_Mobile"},
    {"sectorName": "Barrio 5 de Julio", "hasPower": True, "reportType": "NORMAL", "voltage": 117.0, "deviceOrigin": "CitizenApp_Mobile"},
    {"sectorName": "Ciudad Varyna Los Samanes", "hasPower": True, "reportType": "NORMAL", "voltage": 117.5, "deviceOrigin": "CitizenApp_Mobile"},
    {"sectorName": "Fundacea", "hasPower": False, "reportType": "AVERIA", "voltage": 0.0, "deviceOrigin": "CitizenApp_Mobile"},
    {"sectorName": "Sector La Caramuca", "hasPower": True, "reportType": "NORMAL", "voltage": 117.0, "deviceOrigin": "CitizenApp_Mobile"}
]

def main():
    now_ms = int(time.time() * 1000)
    print("=========================================================")
    print("⚡ SEEDING LUZ BARINAS DATA CATALOG TO FIREBASE FIRESTORE")
    print(f"Project: {PROJECT_ID}")
    print("=========================================================")

    # 1. Seed PAC Schedule Document: app_config/pac_schedule
    print("\n[1/5] Subiendo cronograma oficial PAC y matriz semanal...")
    matrix_rows = [{"cells": row} for row in PAC_MATRIX]
    pac_payload = {
        "version": now_ms,
        "updatedAt": now_ms,
        "matrixRows": matrix_rows,
        "slots": PAC_SLOTS,
        "sectorsA": SECTORS_BLOQUE_A,
        "sectorsB": SECTORS_BLOQUE_B,
        "sectorsC": SECTORS_BLOQUE_C,
        "sectorsD": SECTORS_BLOQUE_D
    }
    pac_res = firestore_patch("app_config", "pac_schedule", to_firestore_fields(pac_payload))
    if pac_res:
        print("  ✓ app_config/pac_schedule actualizado con éxito.")
    else:
        print("  ✗ Fallo al subir app_config/pac_schedule.")

    # 2. Seed Official Broadcast Notice: app_config/broadcast_notice
    print("\n[2/5] Subiendo aviso oficial de Corpoelec Barinas...")
    broadcast_payload = {
        "title": "⚡ Cronograma Oficial PAC Barinas 2026 en Operación",
        "message": "Plan de Administración de Carga activo para los Bloques A, B, C y D. Recuerde desconectar equipos de alto consumo durante el inicio del turno.",
        "level": "INFO",
        "timestamp": now_ms,
        "active": True
    }
    b_res = firestore_patch("app_config", "broadcast_notice", to_firestore_fields(broadcast_payload))
    if b_res:
        print("  ✓ app_config/broadcast_notice publicado con éxito.")

    # 3. Seed Master Sectors Collection: sectors/
    print(f"\n[3/5] Subiendo {len(MASTER_SECTORS)} sectores y circuitos a la colección 'sectors'...")
    success_count = 0
    for s in MASTER_SECTORS:
        clean_id = "sec_" + re.sub(r'[^a-z0-9]', '_', s["name"].lower()).strip('_')
        doc_data = {
            "id": clean_id,
            "name": s["name"],
            "parroquia": s["parroquia"],
            "circuitCode": s["circuit"],
            "rotationBlock": s["block"],
            "status": s["status"],
            "voltage": s["voltage"],
            "confirmedReportsCount": 18 if s["status"] == "SCHEDULED_OUTAGE" else 3,
            "withoutPowerPercentage": 95 if s["status"] == "SCHEDULED_OUTAGE" else 0,
            "lastUpdatedMillis": now_ms
        }
        res = firestore_patch("sectors", clean_id, to_firestore_fields(doc_data))
        if res:
            success_count += 1
            print(f"  ✓ [{s['block']}] {s['name']} ({s['parroquia']})")
        else:
            print(f"  ✗ Error en {clean_id}")
        time.sleep(0.05) # avoid aggressive rate limits

    print(f"  => Total sectores subidos: {success_count}/{len(MASTER_SECTORS)}")

    # 4. Seed Community Submissions: community_locations/
    print(f"\n[4/5] Subiendo {len(COMMUNITY_SUBMISSIONS)} propuestas ciudadanas a 'community_locations'...")
    for c in COMMUNITY_SUBMISSIONS:
        c_doc = {
            "id": c["id"],
            "name": c["name"],
            "parroquia": c["parroquia"],
            "circuitCode": c["circuitCode"],
            "block": c["block"],
            "rotationBlock": c["block"],
            "status": c["status"],
            "voltage": c["voltage"],
            "submittedAt": now_ms - (3600 * 1000 * 4), # 4 hours ago
            "confirmedReportsCount": 2,
            "withoutPowerPercentage": 0
        }
        firestore_patch("community_locations", c["id"], to_firestore_fields(c_doc))
        print(f"  ✓ Propuesta: {c['name']} ({c['parroquia']} - Bloque {c['block']})")

    # 5. Seed Recent Citizen Reports: citizen_reports/
    print(f"\n[5/5] Subiendo {len(CITIZEN_REPORTS)} reportes al feed de 'citizen_reports'...")
    for idx, r in enumerate(CITIZEN_REPORTS):
        r_id = f"rep_seed_{idx + 1}"
        r_doc = {
            "sectorName": r["sectorName"],
            "sectorId": "sec_" + re.sub(r'[^a-z0-9]', '_', r["sectorName"].lower()),
            "hasPower": r["hasPower"],
            "reportType": r["reportType"],
            "voltage": r["voltage"],
            "timestamp": now_ms - (idx * 180000), # staggered every 3 mins
            "deviceOrigin": r["deviceOrigin"]
        }
        firestore_patch("citizen_reports", r_id, to_firestore_fields(r_doc))
        print(f"  ✓ Reporte: {r['sectorName']} -> {'Con Luz' if r['hasPower'] else 'Sin Luz'}")

    print("\n=========================================================")
    print("✅ ¡SEMBRADO DE FIREBASE COMPLETADO CON ÉXITO!")
    print("Todos los sectores, bloques, matriz PAC, avisos y reportes")
    print("están ahora almacenados en la nube de Firebase Firestore.")
    print("=========================================================")

if __name__ == "__main__":
    main()
