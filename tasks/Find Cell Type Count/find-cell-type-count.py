#!/usr/bin/env python3

import hca, json


def count_cell_types(cell_type):
    count = 0
    query = {
        "query" : {
            "bool" : {
                "must" : [{
                    "match" : {
                        "files.biomaterial_json.biomaterials.content.organ.text" : cell_type
                    }
                }, {
                    "match" : {
                        "files.biomaterial_json.schema_version" : "5.1.0"
                    }
                }, {
                    "range" : {
                        "files.biomaterial_json.biomaterials.content.total_estimated_cells" : {
                            "gt" : 0
                        }
                    }
                }]
            }
        }
    }
    
    # Iterate through all search results containing the chosen cell type
    for bundle in hca.dss.DSSClient().post_search.iterate(es_query=query, replica='aws', output_format='raw'):
        
        # Skip over any empty bundles
        if not bundle['metadata'].get('files'):
            continue
        
        # If the checks passed, then increment the count by the number of cells found in this file
        count += bundle['metadata']['files']['biomaterial_json']['biomaterials'][0]['content']['total_estimated_cells']
    return count


def main():
    organ_type = 'spleen'
    print('{} cell count: {}'.format(organ_type, count_cell_types(organ_type)))

    
main()
